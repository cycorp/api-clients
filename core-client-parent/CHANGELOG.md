Cyc Core Client CHANGELOG
=========================

For more information, view the [README](README.md) bundled with this release or visit the
[Cyc Developer Center](http://dev.cyc.com/api/core/).


1.0.0 - 2017-12-12
------------------

1.0.0 release. Future 1.x releases will be backwards-compatible, per the
[Semantic Versioning 2.0.0](https://semver.org/) specification.

_Note that the Base Client is subject to frequent change, and so is not supported for external
developers._


1.0.0-rc9.0 - 2017-12-12
------------------------

Consolidates and streamlines factory methods, in addition to other assorted improvements. It is 
_not backwards-compatible_ with earlier API releases.

This release contains numerous minor improvements and cleanup in preparation for 1.0.0. 


1.0.0-rc8.0 - 2017-12-11
------------------------

Updated to reflect the changes to Cyc Core API 1.0.0-rc8.0. It is _not backwards-compatible_ with 
earlier API releases.

#### Java 1.8

Now requires Java 8 or greater to run, and `JDK 1.8` or greater to build.

#### Query Client

* Adds `com.cyc.query.client.templates.OeTemplateProcessor` for processing TOE-assert templates.

#### Session Client

* Adds `com.cyc.session.CycServerPool` for representing pools of available Cyc servers.


1.0.0-rc7.0 - 2017-07-28
------------------------

Updated to reflect the changes to Cyc Core API 1.0.0-rc7.0. It is _not backwards-compatible_ with 
earlier API releases.

Other changes include:

#### Java 1.7

Now requires Java 7 or greater to run, and `JDK 1.7` or greater to build.

#### KB Client

* Cleans up some implementation code.
* Strengthens some error checking and improves error messages, particularly for `Relation` methods 
  which take varargs.
* `KbObjectImpl#getCore()` can be more tightly typed via generics, and this is leveraged by 
  subclasses. E.g., an `AssertionImpl` has a `CycAssertion`, a `SentenceImpl` has a 
  `FormulaSentence`, a `KbTermImpl` has a `DenotationalTerm`, etc.
* Some minor renamings for accuracy: `KbObjectFactory` is now `KbObjectImplFactory`, `KbObjectTest`
  is now `KbObjectImplTest`, etc.

#### Query Client

* Query Client: implementation classes that were under `com.cyc.query` are now under
  `com.cyc.query.client`.

#### Base Client

* `CycObject` & `KbTool` implementations are now named after their primary interface + "Impl". E.g.,
  `CycFormulaSentence` is now `FormulaSentenceImpl`, `CycObjectTool` is now `ObjectToolImpl`, etc.
* Fixes broken tests.


1.0.0-rc6 - 2017-07-19
----------------------

Implements the new features added to Cyc Core API 1.0.0-rc6:

* Adds support for Query ProofViews.
* Adds support for retrieving Query rules.
* Improves support for working with quoted terms.
* Improves support for working with indexicals and variables.
* Improves support for performing substitutions.

See the CHANGELOG in the Cyc Core API (v1.0.0-rc6) for additional details.

Also:

* Improves equality reasoning between CycLists/Nauts/FormulaSentences.
* Other assorted bug fixes.

1.0.0-rc6 is _not backwards-compatible_ with earlier API releases.


1.0.0-rc5.2 - 2016-01-26
------------------------

* Fixes a ParaphraserFactory-related StackOverflowError in the Base Client 
  (CycClient#commonInitialization()) which occurred when NL API was on the classpath.
* The warning message which is logged when the NL API is missing from the classpath has been toned 
  down & clarified a bit, and is only logged when the user actually attempts to do something 
  NL-related (i.e., when the ParaphraserFactory first attempts to load a Paraphraser).


1.0.0-rc5.1 - 2016-01-18
------------------------

Fixes several bugs in the Core Client implementation:

* QueryImpl#getAnswerSentence() not substituting bindings.
* CycListParser mangling very big integers.
* LegacyCycClientManager#setCurrentSession(CycServerAddress) being ignored.
* UnmodifiableCycList modified by java.util.Collections#sort() under Java 8.

Additionally, in keeping with Google Java Style, references to "ELMt" have been changed to "ElMt".


1.0.0-rc5 - 2015-12-18
----------------------

The fifth release candidate of Cycorp's Java API suite, and the first in which the Core API 
specification and the Core Client implementation are packaged as wholly separate artifacts.

1.0.0-rc5 is _not backwards-compatible_ with earlier API releases. Note that _ResearchCyc 4.0q_ and
_EnterpriseCyc 1.7-preview_ require [server code patching](server-patching.md) for compatibility 
with the 1.0.0-rc5 release.

#### API & implementation division

There is now a clean division between the Core APIs and their reference implementation; they have
been split into two artifacts:

* The implementation-independent Core API specification: `core-api-spec`
* The Core Client reference implementation: `cyc-core-client-impl`

See the CHANGELOG in the Cyc Core API (v1.0.0-rc5) for additional details.

### Other changes

* The Cyc Core API GitHub repository has been renamed and expanded into the 
  [Cyc Java API Suite](https://github.com/cycorp/api-suite) repository, of which `core-api-spec` and
  `core-client` are sub-projects.
* Reorganization of classes (especially exceptions) within the com.cyc.* package space.
* Implementation projects built on the Base Client have had their artifactIds renamed to incorporate
  the word "client". E.g., `cyc-core-suite` is now `cyc-core-client-impl`, `cyc-session` is now 
  `cyc-session-client`, etc.
* Project/module directories have been renamed to mirror their artifactIds, dropping the "cyc-" 
  prefix. E.g., the `cyc-session-client` project lives in the `session-client` directory.
* The `cyc-core-api-parent` and `cyc-core-client-parent` POMs now inherit from `cyc-api-parent`,
  which inherits from `cyc-default-config-parent`.
* Assorted bug fixes.
