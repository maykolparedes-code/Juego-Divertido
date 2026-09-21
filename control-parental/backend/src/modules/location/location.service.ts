import { Injectable } from '@nestjs/common';
import { PrismaService } from '../../common/prisma/prisma.service';
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
    private readonly prisma: PrismaService,
    private readonly encryption: EncryptionService,
    private readonly gateway: LocationGateway,
  ) {}

  async recordPing(input: LocationPingInput) {
    await this.prisma.locationPing.create({
      data: {
        deviceId: input.deviceId,
        encLat: this.encryption.encrypt(String(input.lat)),
        encLng: this.encryption.encrypt(String(input.lng)),
        accuracyMeters: input.accuracyMeters,
        capturedAt: input.capturedAt,
      },
    });

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
    const ping = await this.prisma.locationPing.findFirst({
      where: { deviceId },
      orderBy: { capturedAt: 'desc' },
    });
    if (!ping) return null;

    return {
      deviceId,
      lat: Number(this.encryption.decrypt(ping.encLat)),
      lng: Number(this.encryption.decrypt(ping.encLng)),
      accuracyMeters: ping.accuracyMeters,
      capturedAt: ping.capturedAt,
    };
  }
}
