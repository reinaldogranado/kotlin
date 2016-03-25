# Type aliases

## Goals

* Avoid repetition of types in source code without a cost of introducing extra classes.
    * Function types
    * Collections (and other complex generics)
    * Nested classes
    * ...

## Cons

Java has no notion of "type aliases" and can't see them in class member signatures.

## Syntax

Type aliases are declared using `typealias` keyword:
```
typeAlias
    : modifiers 'typealias' (type '.')? SimpleName typeParameters? '='
        type typeConstraints
```
Type aliases can be top-level object declarations, member declarations, or local decarations.
```
toplevelObject
    : ...
    : typeAlias
    ;

memberDeclaration
    : ...
    : typeAlias
    ;

declartion
    : ...
    : typeAlias
    ;
```

Examples:
* Simple type aliases
```
typealias Int8 = Byte
typealias Int8Array = ByteArray
```
* Generic type aliases
```
typealias Predicate<in T> = (T) -> Boolean
typealias Array2D<T> = Array<Array<T>>
typealias NodeBuilder<T : Any> = T.() -> DocumentationNode
```
* Nested type aliases
```
class MyMap<K, out V> : Map<K, V> {
  typealias EntryCollection = Collection<Map.Entry<K, V>>
  private typealias KVPairs = Array<Pair<K, V>>
  // ...
}
```
* Extension type aliases
```
typealias <K, V> Map<K, V>.Key = K
```

## Abbreviated and unabbreviated types

Type is *abbreviated* if it contains type aliases.
Type alias is *unabbreviated* if it doesn't contain type aliases.

Type aliases can't be recursive (including indirect recursion).
```
typealias A = () -> B // Error: recursive type alias
typealias B = () -> A // Error: recursive type alias
```
Thus for each abbreviated type there is a single unabbreviated type with all type aliases expanded.

For each abbreviated type there is a single classifier corresponding to an unabbreviated type.
Note that such classifier can be obtained without full type aliases expansion.

For generic type aliases, the same rules related to variance and type parameter bounds as for generic classes apply.

### ???

* Should we infer variance and/or bounds for generic type parameters of type aliases?
```
typealias Predicate<T> = (T) -> Boolean
// equivalent to 'typealias Predicate<in T> = (T) -> Boolean
```

## Type aliases and visibility

Type aliases can have the same visibility modifiers as other members of the corresponding scope:
* package-level type aliases can be `public`, `internal`, or `private` (public by default);
* class-level type aliases can be `public`, `internal`, `protected`, or `private` (public by default);
* block-level type aliases are local to a given block.

Type aliases can't expose package or class members with more restricted visibility.
```
class C {
  protected class Nested { ... }
  typealias N = Nested // Error: typealias N exposes class A which is protected in C
}
```
```
internal class Private { ... }
typealias P = Private // Error: typealias P exposes class Private which is internal in module M
```

### ???

* Should type aliases themselves be a subject of effective visibility restrictions?
  Most likely yes, since we don't want abstractions to leak.
```
private typealias A = () -> Unit
typealias B = A // Error: typealias B exposes typealias A which is private in file
```

## Type aliases and resolution

Type aliases are treated as classifiers by resolution.
* When used as type, type alias represents corresponding unabbreviated type.
* When used as value, as function, or as qualifier in a qualified expression, type alias represents corresponding classifier.

https://github.com/orangy/komplex/blob/master/core/src/model/buildGraph.kt
```
typealias ProducersMap = HashMap<ArtifactDesc, BuildGraphNode>
typealias ConsumersMap = HashMap<ArtifactDesc, ArrayList<BuildGraphNode>>

@JvmName("producers_map_contains")
internal fun ProducersMap.contains(artifact: ArtifactDesc, scenario: Scenarios): Boolean = ...
         // typealias used as type
         // HashMap<ArtifactDesc, BuildGraphNode>.contains(...) = ...

@JvmName("consumers_map_contains")
internal fun ConsumersMap.contains(artifact: ArtifactDesc, scenario: Scenarios): Boolean = ...
         // typealias used as type
         // HashMap<ArtifactDesc, ArrayList<BuildGraphNode>>.contains(...) = ...

class BuildGraph() {
    // ...

    val producers = ProducersMap()
               // typelias used as a class constructor
               // : HashMap<ArtifactDesc, BuildGraphNode> = hashMapOf()

    val consumers = ConsumersMap()
               // typelias used as a class constructor
               // : HashMap<ArtifactDesc, ArrayList<BuildGraphNode>>() = hashMapOf()

    // ...
}
```

## Type aliases and reflection

### ???
* Should type aliases be represented as special instances of `kotlin.reflect.KType` at run-time?
    * Equality?
```
typealias Strings = List<String>

fun shorts(ss: Strings) = ss.filter { it.length < 4 }
// NB: shorts(ss: Strings): List<String>

val shortsParamType = ::short.parameters[0].type
val shortsReturnType = ::short.returnType
// NB: shortsParamType == shortsReturnType && shortsParamType !== shortsReturnType
```

* Should there be a special annotation target for type aliases?
* Should annotations with `AnnotationTarget.CLASS` be applicable to type aliases?
    - Looks like no, unless type aliases are represented as synthetic classes at run-time.
