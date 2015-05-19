//
//  PhotoFrameStatus.m
//  SmartHouse
//
//  Created by Anton Bohomol on 4/29/15.
//  Copyright (c) 2015 CYBERVISION INC. All rights reserved.
//

#import "PhotoFrameStatus.h"

@implementation PhotoFrameStatus

- (instancetype)initWithStruct:(kaa_photo_event_class_family_photo_frame_status_update_t *)status {
    self = [super init];
    if (self) {
        if (status->album_id->type == KAA_PHOTO_EVENT_CLASS_FAMILY_UNION_STRING_OR_NULL_BRANCH_0) {
            self.albumId = [NSString stringWithCString:((kaa_string_t*)status->album_id->data)->data encoding:NSUTF8StringEncoding];
        }
        if (status->status->type == KAA_PHOTO_EVENT_CLASS_FAMILY_UNION_SLIDE_SHOW_STATUS_OR_NULL_BRANCH_0) {
            self.status = *(int*)status->status->data;
        }
        if (status->photo_number->type == KAA_PHOTO_EVENT_CLASS_FAMILY_UNION_INT_OR_NULL_BRANCH_0) {
            self.photoNumber = *(int*)status->photo_number->data;
        }
        if (status->thumbnail->type == KAA_PHOTO_EVENT_CLASS_FAMILY_UNION_BYTES_OR_NULL_BRANCH_0) {
            self.thumbnail = [Utils imageFromBytes:status->thumbnail->data];
        }
    }
    return self;
}

@end