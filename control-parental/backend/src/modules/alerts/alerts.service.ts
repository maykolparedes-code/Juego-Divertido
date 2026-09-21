import { Injectable } from '@nestjs/common';
import { PrismaService } from '../../common/prisma/prisma.service';
import { PushService } from '../../common/push/push.service';
import { EncryptionService } from '../../common/encryption/encryption.service';

interface CreateAlertInput {
  familyId: string;
  deviceId: string;
  type: 'SOS' | 'GEOFENCE_ENTER' | 'GEOFENCE_EXIT' | 'SCREEN_TIME_LIMIT_REACHED';
  lat?: number;
  lng?: number;
  message?: string;
}

const ALERT_COPY: Record<CreateAlertInput['type'], { title: string; body: string }> = {
  SOS: { title: 'Alerta SOS', body: 'Tu hijo/a pulsó el botón de pánico' },
  GEOFENCE_ENTER: { title: 'Zona segura', body: 'Entró a una zona segura' },
  GEOFENCE_EXIT: { title: 'Zona segura', body: 'Salió de una zona segura' },
  SCREEN_TIME_LIMIT_REACHED: {
    title: 'Límite de pantalla',
    body: 'Se alcanzó el límite de tiempo configurado',
  },
};

@Injectable()
export class AlertsService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly push: PushService,
    private readonly encryption: EncryptionService,
  ) {}

  async create(input: CreateAlertInput) {
    const alert = await this.prisma.alert.create({
      data: {
        familyId: input.familyId,
        deviceId: input.deviceId,
        type: input.type,
        message: input.message,
        encLat: input.lat != null ? this.encryption.encrypt(String(input.lat)) : undefined,
        encLng: input.lng != null ? this.encryption.encrypt(String(input.lng)) : undefined,
      },
    });

    const copy = ALERT_COPY[input.type];
    const parentPushTokens = await this.getParentPushTokens(input.familyId);
    await Promise.all(
      parentPushTokens.map((token) =>
        this.push.sendAlert(token, copy.title, copy.body, {
          type: input.type,
          deviceId: input.deviceId,
        }),
      ),
    );

    return alert;
  }

  private async getParentPushTokens(familyId: string): Promise<string[]> {
    const devices = await this.prisma.device.findMany({
      where: { familyId, owner: { role: 'PARENT' } },
      select: { pushToken: true },
    });
    return devices
      .map((d) => d.pushToken)
      .filter((token): token is string => Boolean(token));
  }
}
