#include <mbedtls/pk.h>
#include <mbedtls/entropy.h>
#include <mbedtls/ctr_drbg.h>
#include <mbedtls/md.h>
#include <kaa_error.h>
#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>

#define KAA_SESSION_KEY_LENGTH         16

/**
 * @brief generate AES key
 */
int init_aes_key(unsigned char *key, size_t bytes);

/**
 * @brief encrypt or decrypt with AES key
 */
kaa_error_t aes_encrypt_decrypt(int mode, const uint8_t *input, size_t input_size,
        uint8_t *output, const uint8_t *key);

/**
 * @brief create RSA signature
 */
int rsa_sign(mbedtls_pk_context *pk, const uint8_t *input, size_t input_size,
        uint8_t *output, size_t *output_size);
