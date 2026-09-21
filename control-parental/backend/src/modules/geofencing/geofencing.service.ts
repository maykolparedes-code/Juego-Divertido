import { Injectable } from '@nestjs/common';
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
  constructor(private readonly alerts: AlertsService) {}

  async createGeofence(input: CreateGeofenceInput) {
    // Persistencia real: prisma.geofence.create({ data: input })
    return { ...input, id: 'generated-id' };
  }

  async listGeofences(familyId: string) {
    // Persistencia real: prisma.geofence.findMany({ where: { familyId } })
    return [] as unknown[];
  }

  /**
   * Recibe eventos ya calculados en el dispositivo del menor (ver
   * GeofenceBroadcastReceiver.kt) — el servidor no evalúa geometría, solo
   * registra el evento y notifica al padre.
   */
  async recordEvent(input: GeofenceEventInput) {
    // Persistencia real: prisma.geofenceEvent.create({ data: input })

    await this.alerts.create({
      familyId: input.familyId,
      deviceId: input.deviceId,
      type: input.type === 'ENTER' ? 'GEOFENCE_ENTER' : 'GEOFENCE_EXIT',
    });
  }
}
