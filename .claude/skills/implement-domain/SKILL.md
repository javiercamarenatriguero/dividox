---
name: implement-domain
description: Scaffolds the Logic Layer (Domain & Data) including Use Cases, Repositories, Data Sources, Domain Models, and DI wiring. Use when implementing business logic, data access, domain rules, or creating new components following Clean Architecture.
---

# Logic Layer Architect

Scaffold the complete Logic layer (Domain + Data) following Clean Architecture principles.

## 1. Analyze the Request
- Identify the `component-name` (e.g., `user`, `settings`).
- If not provided, ask the user for it.

## 2. Execution Steps

### Step 1: Create Directory Structure
- Create Domain Layer directories:
  - `domain/model`
  - `domain/repository`
  - `domain/usecase`
- Create Data Layer directories:
  - `data/repository`
  - `data/datasource`
  - `data/mapper` (optional)

### Step 2: Create Repository Interface (Domain)
```kotlin
package com.akole.dividox.[component_name].domain.repository

import kotlinx.coroutines.flow.Flow

interface [ComponentName]Repository {
    // Define repository methods here
    // suspend fun getData(): Result<Data>
    // fun observeData(): Flow<Data>
}
```

### Step 3: Create Repository Implementation (Data)
```kotlin
package com.akole.dividox.[component_name].data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class [ComponentName]RepositoryImpl(
    private val ioDispatcher: CoroutineDispatcher,
) : [ComponentName]Repository {
    // Implementation
}
```

### Step 4: Register in DI
```kotlin
single<[ComponentName]Repository> {
    [ComponentName]RepositoryImpl(
        ioDispatcher = get(named(Dispatcher.IO))
    )
}
```

## 3. Verification
- Ensure strict separation: Domain should NOT depend on Data or Android framework.
- Ensure `RepositoryImpl` takes an `ioDispatcher`.

## 4. References
- For dependency rules and clean architecture guidelines, see [references/module-rules.md](references/module-rules.md).
