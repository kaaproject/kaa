---
layout: page
title: Objective-C
permalink: /:path/
sort_idx: 40
---

* TOC
{:toc}

# Naming

Class and File names should follow a consistent pattern of: Feature Type (`Help`) + Descriptive Verb/Noun (`SyncWireless`) + Class Type (`ViewController`).

```objc
HelpSyncWirelessViewController
```

Any classes that require to be subclassed should add `Abstract` as the first component of the Verb/Noun attribute.

```objc
AbstractEventViewController
```

`ENUM` types specific to a class should incorporate the class name, meanwhile omitting the class type in favor for additional descriptive verb/nouns.

```objc
BucketRunnerTaskState
```

# Declarations

Never declare an `ivar` unless you need to change its type from its declared property.

Don’t use line breaks in method declarations.

Prefer exposing an immutable type for a property if it being mutable is an implementation detail. This is a valid reason to declare an `ivar` for a property.

Declare properties readonly if they are only set once in `-init`.

Declare properties copy if they return immutable objects and aren't ever mutated in the implementation.

Don't use `@synthesize` unless the compiler requires it. Note that optional properties in protocols must be explicitly synthesized in order to exist.

Instance variables should be prefixed with an underscore (just like when implicitly synthesized).

Class initializers should generally return `instancetype` rather than `id`.

# Expressions

Don't access an `ivar` unless you're in `-init` or `-dealloc`, custom setters, getters or tight loops.

Use dot syntax when accessing property.

Use object literals, boxed expressions, and subscripting over the older alternatives.

```objc
NSNumber *boolYES = @YES;
NSMutableArray *instrumentsMutable = [@[ @"Ocarina", @"Flute", @"Harp"] mutableCopy];
NSDictionary *stats = @{@"Character": @"Zelda", @"Weapon": @"Sword", @"Hitpoints": @50};
NSString *name = stats[@"Character"];
NSNumber *diameter = @( 2 * pi * r );
```

Comparisons should be explicit for everything except `BOOL`s.

One space between operators and operands.

```objc
NSInteger value = this + that;
```

Prefer positive comparisons to negative.

Short form, `nil` coalescing ternary operators should avoid parentheses.

```objc
Blah *a = thingThatCouldBeNil ?: defaultValue;
```

There shouldn't be a space between a cast and the variable being cast.

```objc
NewType a = (NewType)b;
```

Pointer notation should be applied to the variable and not the type

```objc
NSInteger *counter = 0;
```

# Whitespace

Curly braces general rule - place them on the same line before and with a newline after.

Add a space after the `-` or `+` in the method declaration.

Space between type and `*` in return type and argument type.

No space between the closing paren of the return type and the method name.

No space between the closing paren of the argument type and the argument name.

No tabs or spaces at the end of lines, including empty lines.

Add a space after the type but not the variable name (e.g, `NSString *string`).

End files with a newline.

Make liberal use of vertical whitespace to divide code into logical chunks.

Space between `//` and the first character of a comment.

Spaces for indentation and for alignment within a line, not tabs.

Always surround `if` and `else` bodies with curly braces.

`return` and `break` early at the top of methods or in obvious places.
Try not to place early `return`s in the middle of functions as the control flow is then not clear (you can create result variable, which will get it’s value somewhere in the method and return it).

No spaces between parentheses and their contents.

Insert a space between control structures `if`, `while`, `for`, `return`, etc. and their opening parenthesis but no space between function names and their opening parent.

```objc
- (ReturnType *)methodName:(TypeOfArgument *)arg1 withArgName:(TypeOfArgument *)arg2 {
    if (somethingIsWrong) {
        return;
    }

    if (something == somethingElse) {
        NSString *abcde = @"abcdefg";
        counter++;
    } else {
        [arg2 sendMessageWithArgument:arg1];
    }
    return [arg2 anotherMessage];
}
```

Don't put a space between an object type and the protocol it conforms to.

```objc
@property (attributes) id<Protocol> object;
@property (nonatomic, strong) NSObject<Protocol> *object;
```

C function declarations should have no space before the opening parenthesis, and should be namespaced just like a class.

```objc
void GHAwesomeFunction(BOOL hasSomeArgs);
```

One space after commas in parameter lists.

```objc
NSLog(@"%d %d", param1, param2);
```

# Comments and logical scoping

Use comments beginning with `// TODO`: to denote to-do, bugs and areas of concern.
Allows for quick searches through the code for problems to be solved.
Also Xcode provides some nice formatting for this.

Use the different commenting conventions below for method, multi-line and single-line scoped comments.

Use braces for scoping of begin/end patterns to make clear the begin and end points.

```objc
extern NSString *const kTransactionKey;

// Method level comment
- (ReturnType *)methodName:(TypeOfArgument *)arg1 withArgName:(TypeOfArgument *)arg2 {
    // Block level comment, applies to multiple lines, note the scoping braces below
    BeginTransaction(kTransactionKey);
    {
        // TODO: mk - test this line.
        [arg2 multiArgument:a message:b sentToHere:abcdefg];
    }
    EndTransaction(kTransactionKey);

    return [arg2 anotherMessage];
}

```

# Blocks

Blocks should have a space between their return type and name.

Block definitions should omit their return type when possible.

Block definitions should omit their arguments if they are void.

```objc

void (^blockName1)() = ^{
    // do some things
};

id (^blockName2)(id) = ^id(id args) {
    // do some things
};

```

# Literals

Prefer literals over their more verbose counterparts.

The contents of array and dictionary literals should have a space after commas.

Dictionary literals should have no space between the key and the colon, and a single space between colon and value.

```objc
NSMutableArray *modifiableItems = [@[] mutableCopy];
NSArray *theThing = @[@1, @2, @3];
NSDictionary *keyedThing = @{GHDidCreateStyleGuide: @YES};
```

Longer or more complex literals should be split over multiple lines (optionally with a terminating comma).

```objc
NSArray *theThing = @[
    @"Got some long string objects in here.",
    [AndSomeModelObjects too],
    @"More strings."
];

NSDictionary *keyedThing = @{
    @"this.key": @"corresponds to this value",
    @"otherKey": @"remoteData.payload",
    @"some": @"more",
    @"JSON": @"keys",
    @"and": @"stuff"
};
```

# Categories

Categories should be named for the sort of functionality they provide.

If you need to expose private methods for subclasses or unit testing, create a class extension named `Class+Private`.

# Bit Fields, Enums and Options

Each element of an enum or options should contain naming pattern tying them back to their definition.

The typename should be in the plural while the individual labels should be singular.

Use `NS_OPTIONS` to declare typed options and bit fields.

```objc
typedef NS_OPTIONS(NSUInteger, Arrow) {
    ArrowNone,
    ArrowRight,
    ArrowBottom,
    ArrowLeft,
    ArrowTop,
};
```

Use `NS_ENUM` in place of enum

```objc
typedef NS_ENUM(NSUInteger, UITableViewCellStyle) {
    UITableViewCellStyleValue1,
    UITableViewCellStyleValue2,
    UITableViewCellStyleSubtitle,
};
```

# Constants and Macros

Prefer typed constants to `#define`s; always begin constants with a lower case `k`

```objc
// Preferred
static const NSUInteger kEatActivityHighlightColor = 0x8fdc00;
// Undesirable
#define kEatActivityHighlightColor 0x8fdc00
```

Use macros sparingly preferring classes and categories to macro code expansion.
