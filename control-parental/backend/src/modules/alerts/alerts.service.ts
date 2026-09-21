import { Injectable } from '@nestjs/common';
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
    private readonly push: PushService,
    private readonly encryption: EncryptionService,
  ) {}

  async create(input: CreateAlertInput) {
    const encLat = input.lat != null ? this.encryption.encrypt(String(input.lat)) : undefined;
    const encLng = input.lng != null ? this.encryption.encrypt(String(input.lng)) : undefined;

    // Persistencia real: prisma.alert.create({ data: { ...input, encLat, encLng } })

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
  }

  private async getParentPushTokens(_familyId: string): Promise<string[]> {
    // Implementación real: prisma.device.findMany({ where: { family: { id }, owner: { role: 'PARENT' } } })
    return [];
  }
}
