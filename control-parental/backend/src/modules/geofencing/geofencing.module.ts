import { Module } from '@nestjs/common';
import { GeofencingController } from './geofencing.controller';
import { GeofencingService } from './geofencing.service';
import { AlertsModule } from '../alerts/alerts.module';

@Module({
  imports: [AlertsModule],
  controllers: [GeofencingController],
  providers: [GeofencingService],
})
export class GeofencingModule {}
