import { Injectable } from '@nestjs/common';
import { PrismaService } from '../../common/prisma/prisma.service';

export interface ChildDeviceSummary {
  deviceId: string;
  childName: string;
  lastSeenAt: string | null;
  minutesUsedToday: number;
  topCategory: string;
  insideSafeZone: boolean;
}

/**
 * Agrega, por dispositivo de un hijo, lo que la pantalla principal del
 * padre necesita en una sola llamada: minutos usados hoy, categoría
 * dominante, y si está dentro de alguna zona segura (según el último
 * evento de geocerca registrado).
 */
@Injectable()
export class FamilyService {
  constructor(private readonly prisma: PrismaService) {}

  async getFamilySummary(familyId: string): Promise<ChildDeviceSummary[]> {
    const childDevices = await this.prisma.device.findMany({
      where: { familyId, owner: { role: 'CHILD' } },
      include: { owner: true },
    });

    const startOfDay = new Date();
    startOfDay.setHours(0, 0, 0, 0);

    return Promise.all(
      childDevices.map(async (device) => {
        const usageToday = await this.prisma.appUsageSession.groupBy({
          by: ['category'],
          where: { deviceId: device.id, startedAt: { gte: startOfDay } },
          _sum: { durationSeconds: true },
        });

        const minutesUsedToday = usageToday.reduce(
          (total, row) => total + Math.round((row._sum.durationSeconds ?? 0) / 60),
          0,
        );

        const topCategory =
          usageToday.length > 0
            ? usageToday.reduce((top, row) =>
                (row._sum.durationSeconds ?? 0) > (top._sum.durationSeconds ?? 0) ? row : top,
              ).category
            : 'OTHER';

        const lastGeofenceEvent = await this.prisma.geofenceEvent.findFirst({
          where: { deviceId: device.id },
          orderBy: { occurredAt: 'desc' },
        });

        return {
          deviceId: device.id,
          childName: device.owner.name,
          lastSeenAt: device.lastSeenAt?.toISOString() ?? null,
          minutesUsedToday,
          topCategory,
          insideSafeZone: lastGeofenceEvent?.type === 'ENTER',
        };
      }),
    );
  }
}
