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
//  LoginViewController.m
//  SmartHouse
//
//  Created by Anton Bohomol on 4/8/15.
//  Copyright (c) 2015 CYBERVISION INC. All rights reserved.
//

#import "LoginViewController.h"
#import "AppDelegate.h"

@interface LoginViewController ()

- (void)dismissKeyboard;
- (void)waitAttaching;

@end

@implementation LoginViewController {
    AppDelegate *app;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.loadingView.activityIndicatorViewStyle = UIActivityIndicatorViewStyleWhiteLarge;
    self.loadingView.tintColor = [UIColor greenColor];
    self.loadingView.color = [UIColor grayColor];
    app = [[UIApplication sharedApplication] delegate];
    
    UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self
                                   action:@selector(dismissKeyboard)];
    [self.view addGestureRecognizer:tap];
}

- (void)dismissKeyboard {
    [self.view endEditing:YES];
}

- (IBAction)signIn:(id)sender {
    if ([_loginField hasText] && [_passwordField hasText]) {
        
        [app storeUserName: _loginField.text];
        [app storeUserPass: _passwordField.text];
        
        [app.kaaController attachToUserWithUsername:_loginField.text andPassword:_passwordField.text];
        self.loginField.hidden = YES;
        self.passwordField.hidden = YES;
        self.signInButton.hidden = YES;
        [self dismissKeyboard];
        [self.loadingView startAnimating];
        [self waitAttaching];
    }
}

- (void)waitAttaching {
    if (![app.kaaController isAttached]) {
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
            [self waitAttaching];
        });
    } else {
        UIViewController *viewController = [self.storyboard instantiateViewControllerWithIdentifier:@"SWRevealViewController"];
        [self presentViewController:viewController animated:YES completion:nil];
    }
}

@end
