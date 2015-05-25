//
//  missing_functionality.m
//  SmartHouse
//
//  Created by Anton Bohomol on 4/10/15.
//  Copyright (c) 2015 CYBERVISION INC. All rights reserved.
//

#import <Foundation/Foundation.h>
#include <stdio.h>
#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>
#include <openssl/rsa.h>
#include <openssl/pem.h>
#include <sys/stat.h>
#include "kaa/utilities/kaa_mem.h"
#include "kaa/kaa_common.h"
#include "kaa/platform-impl/posix/posix_file_utils.h"
#include <kaa/platform/ext_key_utils.h>

#define KAA_KEY_STORAGE @"kaa_key.pub"

static char *kaa_public_key = NULL;
static size_t kaa_public_key_length = 0;

static void kaa_generate_pub_key()
{
    const int kBits = 2048;
    const int kExp = 65537;
    
    RSA *rsa = RSA_generate_key(kBits, kExp, 0, 0);
    
    BIO *bio_pem = BIO_new(BIO_s_mem());
    i2d_RSA_PUBKEY_bio(bio_pem, rsa);
    
    kaa_public_key_length = BIO_pending(bio_pem);
    kaa_public_key = (char *) KAA_MALLOC(kaa_public_key_length);
    if (!kaa_public_key) {
        kaa_public_key_length = 0;
        BIO_free_all(bio_pem);
        RSA_free(rsa);
        return;
    }
    BIO_read(bio_pem, kaa_public_key, kaa_public_key_length);
    
    BIO_free_all(bio_pem);
    RSA_free(rsa);
}

static int kaa_init_key()
{
    NSArray *pathArray = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask,YES);
    NSString *documentsDirectory = [pathArray objectAtIndex:0];
    NSString *key_path_string = [documentsDirectory stringByAppendingPathComponent:KAA_KEY_STORAGE];
    NSLog(@"Key: %@", key_path_string);
    const char *key_path = [key_path_string cStringUsingEncoding:NSASCIIStringEncoding];
    
    struct stat stat_result;
    int key_result = stat(key_path, &stat_result);
    
    if (!key_result) {
        bool need_dealloc = false;
        posix_binary_file_read(key_path, &kaa_public_key, &kaa_public_key_length, &need_dealloc);
    } else {
        kaa_generate_pub_key();
        posix_binary_file_store(key_path, kaa_public_key, kaa_public_key_length);
    }
    
    return 0;
}

void ext_get_endpoint_public_key(char **buffer, size_t *buffer_size, bool *needs_deallocation)
{
    KAA_RETURN_IF_NIL3(buffer, buffer_size, needs_deallocation,);
    if (!kaa_public_key)
        kaa_init_key();
    *buffer = kaa_public_key;
    *buffer_size = kaa_public_key_length;
    *needs_deallocation = false;
}

void ext_status_read(char **buffer, size_t *buffer_size, bool *needs_deallocation)
{
}

void ext_status_store(const char *buffer, size_t buffer_size)
{
}

void ext_configuration_read(char **buffer, size_t *buffer_size, bool *needs_deallocation)
{
}

void ext_configuration_store(const char *buffer, size_t buffer_size)
{
}

