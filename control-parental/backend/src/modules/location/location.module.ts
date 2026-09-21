import { Module } from '@nestjs/common';
import { LocationController } from './location.controller';
import { LocationService } from './location.service';
import { LocationGateway } from './location.gateway';
import { EncryptionService } from '../../common/encryption/encryption.service';

@Module({
  controllers: [LocationController],
  providers: [LocationService, LocationGateway, EncryptionService],
})
export class LocationModule {}
