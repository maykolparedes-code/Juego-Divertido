import { Module } from '@nestjs/common';
import { ScreenTimeController } from './screentime.controller';
import { ScreenTimeService } from './screentime.service';
import { AlertsModule } from '../alerts/alerts.module';

@Module({
  imports: [AlertsModule],
  controllers: [ScreenTimeController],
  providers: [ScreenTimeService],
})
export class ScreenTimeModule {}
