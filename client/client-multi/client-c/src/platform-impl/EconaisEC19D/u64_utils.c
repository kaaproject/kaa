
/*
* libgcc/__ashldi3.c
*/

#include <inttypes.h>
#include <stdint.h>
#include <stddef.h>

uint64_t __ashldi3(uint64_t v, int cnt) {
	int c = cnt & 31;
	uint32_t vl = (uint32_t) v;
	uint32_t vh = (uint32_t) (v >> 32);
	if (cnt & 32) {
		vh = (vl << c);
		vl = 0;
	} else {
		vh = (vh << c) + (vl >> (32 - c));
		vl = (vl << c);
	}
	return ((uint64_t) vh << 32) + vl;
}

