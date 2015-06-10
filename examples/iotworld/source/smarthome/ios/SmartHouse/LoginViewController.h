//
//  LoginViewController.h
//  SmartHouse
//
//  Created by Anton Bohomol on 4/8/15.
//  Copyright (c) 2015 CYBERVISION INC. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

@interface LoginViewController : UIViewController

@property(weak,nonatomic) IBOutlet UITextField *loginField;
@property(weak,nonatomic) IBOutlet UITextField *passwordField;
@property(weak,nonatomic) IBOutlet UIButton *signInButton;
@property(weak,nonatomic) IBOutlet UIActivityIndicatorView *loadingView;

- (IBAction)signIn:(id)sender;

@end