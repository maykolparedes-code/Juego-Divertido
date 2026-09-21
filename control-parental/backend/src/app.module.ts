import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { PrismaModule } from './common/prisma/prisma.module';
import { AuthModule } from './modules/auth/auth.module';
import { LocationModule } from './modules/location/location.module';
import { GeofencingModule } from './modules/geofencing/geofencing.module';
import { ScreenTimeModule } from './modules/screentime/screentime.module';
import { AlertsModule } from './modules/alerts/alerts.module';
import { UsageReportsModule } from './modules/usage-reports/usage-reports.module';
import { FamilyModule } from './modules/family/family.module';

@Module({
  imports: [
    ConfigModule.forRoot({ isGlobal: true }),
    PrismaModule,
    AuthModule,
    LocationModule,
    GeofencingModule,
    ScreenTimeModule,
    AlertsModule,
    UsageReportsModule,
    FamilyModule,
  ],
})
export class AppModule {}
