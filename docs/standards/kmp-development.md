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

## 6. Test Naming (Kotlin Native Compatibility)
- **Constraint**: Do not use parentheses `()` or other special characters besides spaces and underscores in backticked test function names.
- **Reason**: Certain Native targets (e.g., iOS, Linux) will fail to compile tests with these characters in the symbol names.
