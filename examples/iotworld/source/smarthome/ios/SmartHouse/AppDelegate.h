//
//  AppDelegate.h
//  SmartHouse
//
//  Created by Anton Bohomol on 4/3/15.
//  Copyright (c) 2015 CYBERVISION INC. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "KaaController.h"
#import "MusicDeviceController.h"
#import "PhotoDeviceController.h"

@interface AppDelegate : UIResponder <UIApplicationDelegate>

@property(strong,nonatomic) UIWindow *window;
@property(strong,nonatomic) KaaController *kaaController;
@property(strong,nonatomic) MusicDeviceController *musicDeviceController;
@property(strong,nonatomic) PhotoDeviceController *photoDeviceController;

- (void)storeUserName:(NSString *) userName;
- (NSString *)loadUserName;

- (void)storeUserPass:(NSString *) password;
- (NSString *)loadUserPass;

@end

