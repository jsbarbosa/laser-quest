#include <avr/io.h>
#include <stdlib.h>
#include <util/delay.h>

#define F_PWM 50
#define ESCALER 1

#define MIN 400
#define MAX 2200
#define RANGE (MAX - MIN)
#define SLOPE (RANGE/180.0)

#define H_MIN_ANGLE 90 - 15
#define H_MAX_ANGLE 90 + 65

#define V_MIN_ANGLE 90 - 65
#define V_MAX_ANGLE 90 + 30

#define STEP_H 10
#define STEP_V 10

void setAngleH(float angle);
void setAngleV(float angle);

float _random(void);
void stepH(void);
void stepV(void);
uint16_t adc_read(uint8_t adcx);

uint8_t CURRENT_H, CURRENT_V;

int main (void) 
{
	DDRB |= _BV(PB1) | _BV(PB2);

	ICR1 = F_CPU/(ESCALER*F_PWM) - 1;

	TCCR1A |= _BV(COM0A1) | _BV(COM0B1) | _BV(WGM11);
	TCCR1B |= _BV(WGM13) | _BV(WGM12) | _BV(CS10);
	
	setAngleH(H_MIN_ANGLE);
	setAngleV(V_MIN_ANGLE);
	_delay_ms(1000);
	
	DDRC = 0x00;
	ADCSRA |= _BV(ADEN);
	
	srand(adc_read(3));
	
	while(1)
	{
		stepH();
		stepV();
		_delay_ms(250);
	}
}

void setAngleH(float angle)
{
	if (angle >= H_MAX_ANGLE)
	{
		angle = H_MAX_ANGLE;
	}
	else if(angle <= H_MIN_ANGLE)
	{
		angle = H_MIN_ANGLE;
	}
	CURRENT_H = angle;
	OCR1A = CURRENT_H*SLOPE + MIN;
}

void setAngleV(float angle)
{
	if (angle >= V_MAX_ANGLE)
	{
		angle = V_MAX_ANGLE;
	}
	else if(angle <= V_MIN_ANGLE)
	{
		angle = V_MIN_ANGLE;
	}
	CURRENT_V = angle;
	OCR1B = CURRENT_V*SLOPE + MIN;
}

uint16_t adc_read(uint8_t adcx)
{
	ADMUX	&=	0xf0;
	ADMUX	|=	adcx;

	ADCSRA |= _BV(ADSC);

	while ( (ADCSRA & _BV(ADSC)) );
	
	return (uint16_t) ADC;
}

float _random(void)
{
	float temp = rand() / (float) RAND_MAX;
	return 2*(temp - 0.5);
}

void stepH(void)
{
	setAngleH(_random()*STEP_H + CURRENT_H);
}

void stepV(void)
{
	setAngleV(_random()*STEP_V + CURRENT_V);
}
