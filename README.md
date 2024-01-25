# Anvil Core

This is the base package for creating an Anvil-Test-Framework developed for testing protocols. It provides functions for creating and running combinatorial tests using JUnit and Coffee4j as well as storing the results. It is compatible with the Anvil Web UI for analyzing the test results.

Notable implementations are:

- [TLS-Anvil](https://github.com/tls-attacker/tls-anvil)
- [SSH-Anvil](https://github.com/tls-attacker/SSH-Anvil/)
- [X.509-Anvil](https://github.com/tls-attacker/x509-Anvil)

## Integration

To use this framework in your project you need to import it as maven dependency.

In your pom.xml you can reference it as follows:

```xml
<dependency>
    <groupId>de.rub.nds</groupId>
    <artifactId>anvil-core</artifactId>
    <version>[VERSION]</version>
</dependency>
```

If you use the [Protocol-Toolkit-BOM](https://github.com/tls-attacker/Protocol-Toolkit-BOM) you can omit the version attribute.

## Implementation

Anvil-Core relies heavily on coffee4j for combinatorial testing. Before you start you should read the [quick start guide of coffee4j](https://coffee4j.github.io/).

Similar to Coffee4J, you can write combinatorial tests here, as well as normal, simple tests. However, with Anvil-Core you need to declare every parameter type and its possible values beforehand in code.

### Quick Start

To create your own Anvil Integration you will have to:

- Implement your ParameterTypes as enum
  - (opt. implement ParameterScopes)
  - (opt. for every type, create a DerivationParameter class)
  - every type should return an instance of a DerivationParemeter, that itself should return a list of all its parameter values

```java
public enum MyProtocolParameterType implements ParameterType {
    
    MY_PARAMETER_TYPE(MyDerivation.class)
  
    @Override
    public DerivationParameter getInstance(ParameterScope parameterScope) {
        return new MyDerivation();
    }
}
```

- Create a PrameterIdentifierProvider
  - returns all your Types as ParameterIdentifiers here
  - a ParameterIdentifier is a combination of ParameterType and an optional Scope

```java
public class MyProtocolParameterIdentifierProvider extends ParameterIdentifierProvider {
	
    @Override
    public List<ParameterIdentifier> generateAllParameterIdentifiers() {
        List<ParameterIdentifier> allMyParameters = new ArrayList();
        allMyParameters.add(new ParameterIdentifier(MY_PARAMETER_TYPE));
        return allMyParameters;
    }
}
```

- Create your own tests templates
  - annotate with @AnvilTest or @NonCombinatorialAnvilTest
  - each test should create a AnvilTestCase object, where you store your result

```java
public class MyTestClass extends CombinatorialAnvilTest {

    @AnvilTest(id = "myId_1")
    public void myTest(ArgumentsAccessor argumentAccessor) {
        ParameterCombination paraComb = ParameterCombination.fromArgumentsAccessor(argumentAccessor, new DerivationScope(extensionContext));
        // do the test
        AnvilTestCase result = new AnvilTestCase(paraComb, context);
        result.setTestResult(...);
    }
}
```

- Fill in a AnvilConfig object
- Call the TestRunner with the config

```java
AnvilTestConfig anvilConfig = new AnvilTestConfig();
// configure
TestRunner runner = new TestRunner(anvilConfig, "", new MyProtocolParameterIdentifierProvider());
runner.runTests();
```

**More details can be found in the wiki under "Integration" (WIP).**

## Development

Anvil-Core is an open source project and participation is welcome. To set up the development-environment, you will find help here: <https://github.com/tls-attacker/TLS-Attacker-Description>

**To learn more about how Anvil-Core works please read the wiki page under "Development" (WIP).**
