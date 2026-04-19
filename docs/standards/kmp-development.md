# KMP Development Standard (kmp-development.md)
---
name: kmp-development
---

## 1. SQLDelight (.sq files)
- **Mandatory Imports**: Always import kotlin.Boolean, kotlin.String, and collections if used as types.
- **Adapter Naming**: Constructor parameters MUST be named [fieldName]Adapter.

## 2. Ktor 3.x Networking
- **Content-Type**: Prefer header(HttpHeaders.ContentType, ...) over contentType() extension.

## 3. Serialization
- **Explicit Typing**: Provide explicit type parameters to JsonColumnAdapter to prevent inference failure.

## 4. Native Target Considerations
- **persistence**: Separate logic into Sql* and Noop* LocalDataSources.

## 5. Coroutine Dispatchers
- **Prohibition**: Never use Dispatchers.IO in commonMain. Use Dispatchers.Default.
