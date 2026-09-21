import { Body, Controller, Get, Param, Post } from '@nestjs/common';
import { IsDateString, IsNumber, IsString, Max, Min } from 'class-validator';
import { LocationService } from './location.service';

class LocationPingDto {
  @IsString()
  deviceId!: string;

  @IsString()
  familyId!: string;

  @IsNumber() @Min(-90) @Max(90)
  lat!: number;

  @IsNumber() @Min(-180) @Max(180)
  lng!: number;

  @IsNumber()
  accuracyMeters!: number;

  @IsDateString()
  capturedAt!: string;
}

@Controller('location')
export class LocationController {
  constructor(private readonly location: LocationService) {}

  @Post('ping')
  async ping(@Body() dto: LocationPingDto) {
    await this.location.recordPing({
      ...dto,
      capturedAt: new Date(dto.capturedAt),
    });
    return { ok: true };
  }

  @Get(':deviceId/last')
  async last(@Param('deviceId') deviceId: string) {
    return this.location.getLastKnownLocation(deviceId);
  }
}
