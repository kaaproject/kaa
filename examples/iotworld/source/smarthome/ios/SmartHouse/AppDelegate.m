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
