//
//  MusicListItem.m
//  SmartHouse
//
//  Created by Anton Bohomol on 4/17/15.
//  Copyright (c) 2015 CYBERVISION INC. All rights reserved.
//

#import "MusicListItem.h"

@implementation MusicListItem

- (BOOL)isEqual:(id)object {
    if (object == self)
        return YES;
    if (!object || ![object isKindOfClass:[self class]])
        return NO;

    return memcmp(self.endpointId, ((MusicListItem*)object).endpointId, KAA_ENDPOINT_ID_LENGTH) == 0;
}

@end
