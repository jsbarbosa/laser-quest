#include <avr/io.h>
#include <stdlib.h>
#include <util/delay.h>

#define F_CPU 1000000UL
#define F_PWM 50
#define ESCALER 1

#define MIN 400
#define MAX 2200
#define RANGE (MAX - MIN)
#define SLOPE (RANGE/180.0)

#define STEP_H 10

void setAngle1A(float angle);
float _random(void);
void stepH(void);

uint8_t CURRENT_H;

int main (void) 
{
	DDRB |= _BV(PB1);

	ICR1 = F_CPU/(ESCALER*F_PWM) - 1;

	TCCR1A |= _BV(COM0A1) | _BV(WGM11);
	TCCR1B |= _BV(WGM13) | _BV(WGM12) | _BV(CS10);
	
	setAngle1A(90);
	_delay_ms(1000);
	
	while(1)
	{
		stepH();
		//~ _delay_ms(100);
	}
}

void setAngle1A(float angle)
{
	if (angle >= 180)
	{
		angle = 180;
	}
	else if(angle <= 0)
	{
		angle = 0;
	}
	CURRENT_H = angle;
	OCR1A = CURRENT_H*SLOPE + MIN;
}

float _random(void)
{
	float temp = rand() / (float) RAND_MAX;
	return 2*(temp - 0.5);
}

void stepH(void)
{
	setAngle1A(_random()*STEP_H + CURRENT_H);
}
