# CS 1657: Privacy in the Electronic Society

## Project 2

Released: Monday, Mar 28

Due: Sunday, Apr 10, 11:59 PM

### Motivation

In this course, we discussed several approaches to representing and enforcing
access control policies. In this project, you will implement and test a
prototype enforcement engine for an access control policy language of your
choice. Along the way, you will consider benefits and drawbacks to including
advanced features.

Your submission should consist of the following components.

- Code that satisfies the C tasks below, in the language of your choice (check
  with your instructor if you’re not sure it will work for them)
- A writeup that completes the W tasks below **and** discusses your approach to
  the C tasks, mentioning specific lines, function names, etc. to help me
  understand your code. Each task should be discussed in your writeup.

All features, bugs, and other details regarding your code should be made clear
in your writeup (i.e., do not submit a separate README or expect us to read
every comment in your code). Each writing task should be clearly titled, and
each code task should be clearly discussed in the writeup. In short, do not make
us search for the components of your submission. Show off the hard work you did!

You may work individually or in a pair (a group of 2). If you work in a pair,
make sure both members use the GitHub Classroom link to join the repository.
(One member should create the team, then the second should join the existing
team.)

**Note:** I recommend that you think about a language that makes sense in *one
specific type of scenario,* rather than a language that will work for everyone.
Think about things like the following:

- A theater scenario, where ticketholders access seats and concessions, actors
  and other staff access backstage areas, etc.
- A self-driving car, where different processes in the car have access to
  different signals, the manufacturer collects some information for debugging
  purposes, etc.
- An online newspaper, where journalists and reporters can submit their stories,
  sub-editors can modify stories for grammar, photographers can add images, and
  editors can add headlines and approve stories for publication.

Any of these specific examples will make a much more interesting (and
manageable) project than, “a company with 1000 employees and hierarchical
supervision.”

### Tasks

**Task W0:** Choose an access control language that supports indirection,
administrative delegation, and at least one other feature. You may consider
forms of ABAC, ReBAC, SD3, or RT, but you are **not** limited to these options!
I would be happy for you to choose something unique and impressive!

Your language must support at least the following features:

- *Indirection*: The ability to assign a large set of permissions at once.
  Consider roles, attributes, groups, etc.
- *Administrative Delegation*: The ability to set additional entities that have
  the ability to affect a resource's access policy. For example, consider RT's
  delegation of attribute authority, or the $ notation in SD3. You might
  represent different administrative entities as different text files, each
  representing the relations published by one particular entity.
- *At least one other*: At least one other feature that allow administrators to
  write expressive policies. Consider role inheritance (role hierarchy),
  attribute intersection, parameterized attributes, etc.

You may include more than the required features for extra credit.

To start your writeup, overview the access control system that you chose, the
features it supports, and any extra tools you developed.

**Task W1:** Fully describe the syntax used to write a policy file for your
access control language. This file should be plaintext and simple for an
administrator to write. This means that your policy should *not* be hardcoded in
the program. I should be able to adjust the policy without changing or
recompiling the code.

Explain how each supported feature is represented in plaintext. If your file
needs to include a list of all existing users/subjects or
files/resources/objects, explain the format for this information as well. Your
format is allowed to consist of multiple files (e.g., delegated attributes may
be declared in separate files).

If your access control system is inspired by an existing language, note that the
syntax used in your policy file may differ from how we viewed example policies
in class. For instance, you may use `<-` or `:-` to represent RT's `←` symbol.
Alternatively, you may consider standard formats for structured input, which
would enable you to utilize existing parsing libraries. Consider JSON, XML,
Datalog, etc. Again, you **do not** need to type your policies in the exact
format that existing languages use.

To give one simple example using XML, you might represent `Pitt.student ←
Juliana` as follows:

    <attribute name="student" domain="Pitt" constant="Juliana">

**Task C2:** Implement a program to interpret your access control language. It
should be able to do the following steps:

1. Read, parse, and preprocess (if necessary) a specified policy file(s) in the
   format you describe in Task W1.
2. Prompt the user for an access query (that is, a query of the form, “Can user
   *u* access file *f* with *read* privilege?” or similar). Clearly state the
   format in which the user must type their query.
3. Using the policy that was parsed from the input file, determine whether the
   access should be permitted, and output the response (*allow* or *deny*).
4. Repeat from Step 2.

Note that you are only required to support access queries, but you can implement
additional queries for extra credit (e.g., “Is user *u* a member of role *r*?”).

**Task W3:** Explain, with examples, how *indirection* is represented, and how
it should be used, in your language. You should give at least 2 examples to make
it clear how this feature works and how it can be used.

Discuss the benefits to having this feature, from the perspective of privacy,
expressiveness, efficiency, administrative overhead, usability, or some other
metric. Does it allow for easier assignment of certain types of permissions?
Will it prevent errors in the policy? Does it allow users to set policies that
more closely match their preferences? Does it allow resource owners to set
policies they couldn't without the feature, and therefore support more diverse
types of privacy requirements?

Next, discuss the potential drawbacks of having this feature, again considering
privacy, administrative overhead, usability, and/or other metrics. Could it make
it slower to render access decisions? Could it give the user too many options
and potentially overwhelm them? Could it be misunderstood and lead users into
granting accesses they didn't intend to? Could it prevent, or make more complex,
an automated analysis to detect insecure policies? Consider specific scenarios
wherever possible.

**Task W4:** Explain, with examples, how *administrative delegation* is
represented and used in your language. Again, you should give at least 2
examples.

Discuss the benefits and potential drawbacks of having this feature, similar to
how you did in Task W3.

**Task W5:** Name any other access control features that you implemented in your
program (at least one is required). Explain, with examples, how they are
represented and used in your language. As before, give at least 2 examples of
each feature.

Discuss the benefits and potential drawbacks of having each additional feature,
similar to how you did in Task W3.

**Task C6:** Construct at least 5 *experimental policies* (manually written or
randomly generated) of varying size/complexity. Your goal is to study the
efficiency of enforcing policies of various complexities to investigate how your
enforcement program scales to large, complicated policies. Consider, for
instance, long chains of trust.

Write a program to time (benchmark) your processing of a policy and query. This
program should not be interactive and should run hardcoded (or randomly
generated) queries on the policies you constructed, then report the results.

You might consider measuring separately the time for two distinct tasks for each
policy, if it makes sense for the implementation you wrote:

1. The time it takes your program to read and preprocess the policy, without
   considering queries.
2. The time it takes your program to respond to a query *after* the policy has
   been read and processed.

You are welcome to adjust this according to your particular implementation
strategy; what makes the most sense to get a detailed picture of your
implementation's efficiency as policies scale?

Use the average of multiple trials if your timing is not consistent.

**Task W7:** Interpret the results of Task C6. Show a chart, table, or graph of
your data. Explain what this tells you about how your enforcement engine scales.
If the runtime scales poorly, is this a problem with the language you chose, or
with your specific enforcement engine? Is this language reasonable to use for
personal computers? What about enterprise scenarios? Can it scale to a large
cloud storage service? Show off as much as you can regarding what this
experiment taught you about access control enforcement.

### Language / Feature Choice

Up to 10 bonus points will be awarded for choosing an interesting access control
language and incorporating more than the required features. More points will be
awarded for selections that more thoughtful, interesting, creative, clever, etc.

For instance, you may consider developing a companion utility that generates
random policies given particular measures (e.g., number of users per RBAC role,
or denseness of the ReBAC graph). Perhaps you develop a visualization technique
to display complex policies graphically. Maybe you provide intelligent
suggestions for policy changes (e.g., “Warning: Line 15 has no effect due to
redundance with line 10,” or “Error: Line 12 is invalid, did you mean
*[something else]* ?”). Remember that your goal is to show off what our course
has taught you about access control enforcement.

### Grading

| Task      | Points        |
| ----      | ------        |
| Task W0   | 10 (bonus)    |
| Task W1   | 10            |
| Task C2   | 20            |
| Task W3   | 12            |
| Task W4   | 12            |
| Task W5   | 16            |
| Task C6   | 15            |
| Task W7   | 15            |
| **Total** | **Up to 110** |

### Note

As this course is an upper-level elective, you are being given a lot of freedom
in terms of how you tackle this project. In exchange, you also have a lot of
responsibility to demonstrate your hard work adequately to your TA and
instructor. As such, there are tasks in this assignment that require you to
discuss your code in detail. Your discussion should closely align to, and refer
to, your specific implementation. Do not claim that your code does something
that you know it does not (see the Academic Integrity Policy).

In this project, you may use any programming language and libraries that you
prefer. Cite all sources. For instance, you may use parsing libraries to
interpret the text policy and store it in easier-to-manage data structures. I
recommend against using existing libraries that are built to deduce access
control decisions or automatically interpret datalog (unless your project has
sufficient technical depth aside from these components). This is a 2-week
project in an upper-level technical course; if you satisfy the requirements in
only 4 lines of code, you’re probably using a library function that allows you
to skip the hardest (and most interesting!) parts of the assignment. If you need
help deciding whether something should be permitted, contact your instructor.

### Submission

Your writeup should be in PDF format. Submit it, and your code, by committing
and pushing to your GitHub Classroom repository by the date and time listed
above. Late submissions will not be accepted.

