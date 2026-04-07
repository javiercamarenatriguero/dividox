# Module Rules & Best Practices

> For the full multi-module architecture (app/feature/component/integration/common), see `skill: module-organization`.

## Clean Architecture Guidelines within `:component:[name]`

### Domain Layer (`domain/`)
- **Pure Kotlin**: No Android/platform dependencies.
- **Use Cases**: Encapsulate business logic. Single responsibility.
- **Repository Interfaces**: Defined here.
- **Models**: Domain entities.

### Data Layer (`data/`)
- **Repository Implementation**: Implements domain interface.
- **Data Sources**: Handle local DB, API, etc.
- **Mappers**: Map between Data models (DTOs) and Domain models.
- **Dependencies**: Takes `CoroutineDispatcher` (usually IO) in constructor.

## Anti-Patterns
- **Direct UI Dependency**: A component should never know about presentation layer.
- **God Components**: Keep components focused. Split if too large.
- **UI Code in Domain**: No Composables or ViewModels in domain/data modules.

## Troubleshooting Dependencies

### Circular Dependency (A <-> B)
**Solution**: Create an interface that abstracts the interaction.

### Feature A needs Feature B Data
**Solution**: Extract shared logic to a shared component.
- **Don't**: `featureA` -> `featureB`
- **Do**: `featureA` -> `shared` <- `featureB`
