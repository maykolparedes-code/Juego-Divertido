import { Injectable } from '@nestjs/common';
import { PrismaService } from '../../common/prisma/prisma.service';
import { AlertsService } from '../alerts/alerts.service';

interface CreateGeofenceInput {
  familyId: string;
  name: string;
  lat: number;
  lng: number;
  radiusMeters: number;
}

interface GeofenceEventInput {
  geofenceId: string;
  deviceId: string;
  familyId: string;
  type: 'ENTER' | 'EXIT';
}

@Injectable()
export class GeofencingService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly alerts: AlertsService,
  ) {}

  async createGeofence(input: CreateGeofenceInput) {
    return this.prisma.geofence.create({ data: input });
  }

  async listGeofences(familyId: string) {
    return this.prisma.geofence.findMany({ where: { familyId } });
  }

  /**
   * Recibe eventos ya calculados en el dispositivo del menor (ver
   * GeofenceBroadcastReceiver.kt) — el servidor no evalúa geometría, solo
   * registra el evento y notifica al padre.
   */
  async recordEvent(input: GeofenceEventInput) {
    await this.prisma.geofenceEvent.create({
      data: {
        geofenceId: input.geofenceId,
        deviceId: input.deviceId,
        type: input.type,
      },
    });

    await this.alerts.create({
      familyId: input.familyId,
      deviceId: input.deviceId,
      type: input.type === 'ENTER' ? 'GEOFENCE_ENTER' : 'GEOFENCE_EXIT',
    });
  }
}
