import { Injectable } from '@nestjs/common';
import { EncryptionService } from '../../common/encryption/encryption.service';
import { LocationGateway } from './location.gateway';

interface LocationPingInput {
  deviceId: string;
  familyId: string;
  lat: number;
  lng: number;
  accuracyMeters: number;
  capturedAt: Date;
}

@Injectable()
export class LocationService {
  constructor(
    private readonly encryption: EncryptionService,
    private readonly gateway: LocationGateway,
  ) {}

  async recordPing(input: LocationPingInput) {
    const encLat = this.encryption.encrypt(String(input.lat));
    const encLng = this.encryption.encrypt(String(input.lng));

    // Persistencia real: prisma.locationPing.create({ data: { ...encLat, encLng } })

    // Empuja el punto en tiempo real a los padres conectados por WebSocket,
    // sin esperar a que refresquen el mapa manualmente.
    this.gateway.broadcastToFamily(input.familyId, 'location:update', {
      deviceId: input.deviceId,
      lat: input.lat,
      lng: input.lng,
      accuracyMeters: input.accuracyMeters,
      capturedAt: input.capturedAt,
    });
  }

  async getLastKnownLocation(deviceId: string) {
    // Implementación real: leer el último LocationPing y desencriptar.
    return { deviceId };
  }
}
