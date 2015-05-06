//
//  NavigationViewController.m
//  SmartHouse
//
//  Created by Anton Bohomol on 4/6/15.
//  Copyright (c) 2015 CYBERVISION INC. All rights reserved.
//

#import "NavigationViewController.h"
#import "SWRevealViewController.h"

@interface NavigationViewController ()

@end

@implementation NavigationViewController {
    NSArray *menu;
    AppDelegate *app;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    menu = @[@"house", @"music", @"photo", @"logout"];
    app = [[UIApplication sharedApplication] delegate];
    UIView *cellNib = [[[NSBundle mainBundle] loadNibNamed:@"SlidePaneHeader" owner:self options:nil] objectAtIndex:0];
    ((UILabel *)[cellNib viewWithTag:1]).text = [app loadUserName];
    self.tableView.tableHeaderView = cellNib;
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return [menu count];
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    if (indexPath.row == 3) {
        [app storeUserName:nil];
        [app storeUserPass:nil];
    }
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    
    NSString *cellIdentifier = [menu objectAtIndex: indexPath.row];
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier: cellIdentifier forIndexPath:indexPath];
    cell.imageView.image = [UIImage imageNamed:cellIdentifier];
    return cell;
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    if ([segue isKindOfClass: [SWRevealViewControllerSegue class]]) {
        SWRevealViewControllerSegue *swSegue = (SWRevealViewControllerSegue *)segue;
        swSegue.performBlock = ^    (SWRevealViewControllerSegue* rvc_segue, UIViewController* svc, UIViewController* dvc) {
            
            UINavigationController* navController = (UINavigationController*)self.revealViewController.frontViewController;
            [navController setViewControllers: @[dvc] animated: NO];
            [self.revealViewController setFrontViewPosition: FrontViewPositionLeft animated: YES];
        };
    }
}

@end
