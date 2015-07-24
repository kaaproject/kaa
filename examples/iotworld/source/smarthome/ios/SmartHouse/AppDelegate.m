/*
 * Copyright 2014-2015 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//
//  AppDelegate.m
//  SmartHouse
//
//  Created by Anton Bohomol on 4/3/15.
//  Copyright (c) 2015 CYBERVISION INC. All rights reserved.
//

#import "AppDelegate.h"

#define USER_NAME_KEY @"user_name"
#define PASSWORD_KEY @"user_password"

@interface AppDelegate ()

@end

@implementation AppDelegate {
    NSUserDefaults *preferences;
}

- (void)storeUserName:(NSString *)userName {
    [preferences setObject:userName forKey:USER_NAME_KEY];
    [preferences synchronize];
}

- (void)storeUserPass:(NSString *)password {
    [preferences setObject:password forKey:PASSWORD_KEY];
    [preferences synchronize];
}

- (NSString *)loadUserName {
    if (![preferences objectForKey:USER_NAME_KEY]) {
        return nil;
    }
    return [preferences stringForKey:USER_NAME_KEY];
}

- (NSString *)loadUserPass {
    if (![preferences objectForKey:PASSWORD_KEY]) {
        return nil;
    }
    return [preferences stringForKey:PASSWORD_KEY];
}

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions{
    preferences = [NSUserDefaults standardUserDefaults];
    _kaaController = [[KaaController alloc] init];
    [_kaaController initController];
    self.musicDeviceController = [[MusicDeviceController alloc] initWithKaa:self.kaaController];
    self.photoDeviceController = [[PhotoDeviceController alloc] initWithKaa:self.kaaController];
    if ([self loadUserName] && [self loadUserPass]) {
        UIStoryboard *storyboard = self.window.rootViewController.storyboard;
        self.window.rootViewController = [storyboard instantiateViewControllerWithIdentifier:@"SWRevealViewController"];
        [_kaaController attachToUserWithUsername:[self loadUserName] andPassword:[self loadUserPass]];
    }
    
    return YES;
}

- (void)applicationWillTerminate:(UIApplication *)application {
    [_kaaController destroyController];
}

- (void)applicationWillResignActive:(UIApplication *)application {
}

- (void)applicationWillEnterForeground:(UIApplication *)application {
}

- (void)applicationDidEnterBackground:(UIApplication *)application {
}

- (void)applicationDidBecomeActive:(UIApplication *)application {
}

@end
