import { Body, Controller, Get, Param, Post } from '@nestjs/common';
import { AppCategory } from '@prisma/client';
import { IsEnum, IsInt, IsOptional, IsString, Min } from 'class-validator';
import { ScreenTimeService } from './screentime.service';

class CreateRuleDto {
  @IsString() familyId!: string;
  @IsOptional() @IsString() deviceId?: string;
  @IsOptional() @IsString() appPackage?: string;
  @IsOptional() @IsEnum(AppCategory) category?: AppCategory;
  @IsInt() @Min(0) dailyLimitMinutes!: number;
  @IsOptional() @IsString() scheduleStart?: string;
  @IsOptional() @IsString() scheduleEnd?: string;
}

@Controller('screen-time')
export class ScreenTimeController {
  constructor(private readonly screenTime: ScreenTimeService) {}

  @Post('rules')
  createRule(@Body() dto: CreateRuleDto) {
    return this.screenTime.createRule(dto);
  }

  @Get('rules/device/:deviceId')
  listForDevice(@Param('deviceId') deviceId: string) {
    return this.screenTime.listRulesForDevice(deviceId);
  }
}
