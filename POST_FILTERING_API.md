# Post Filtering API

## Endpoint

`GET /posts/filter`

## Description

This endpoint allows filtering posts based on multiple criteria with pagination support.

## Query Parameters

### Filter Criteria

| Parameter              | Type    | Required | Description                                                                                             | Example                               |
| ---------------------- | ------- | -------- | ------------------------------------------------------------------------------------------------------- | ------------------------------------- |
| `captions`             | String  | No       | Filter by substring match in post captions                                                              | `"hello world"`                       |
| `status`               | Integer | No       | Filter by post status:<br/>0: hidden<br/>1: visible<br/>2: sensitive/violent content                    | `1`                                   |
| `audience`             | String  | No       | Filter by audience type:<br/>PUBLIC<br/>PRIVATE                                                         | `PUBLIC`                              |
| `postedAt`             | String  | No       | Filter by specific posted date/time. Supports formats:<br/>- `yyyy-MM-dd'T'HH:mm:ss`<br/>- `yyyy-MM-dd` | `2024-01-15T10:30:00` or `2024-01-15` |
| `isCommentVisible`     | Boolean | No       | Filter posts by comment visibility                                                                      | `true`                                |
| `isLikeVisible`        | Boolean | No       | Filter posts by like visibility                                                                         | `false`                               |
| `commentCount`         | Long    | No       | Filter by comment count comparison                                                                      | `10`                                  |
| `commentCountOperator` | String  | No       | Comparison operator for comment count:<br/>`>`, `<`, `=`, `>=`, `<=`                                    | `>`                                   |

### Pagination Parameters

| Parameter | Type    | Required | Default | Description              |
| --------- | ------- | -------- | ------- | ------------------------ |
| `page`    | Integer | No       | `0`     | Page number (0-based)    |
| `size`    | Integer | No       | `10`    | Number of items per page |

## Response Format

```json
{
  "content": [
    {
      "id": "post-id",
      "captions": "Post content",
      "status": 1,
      "audience": "PUBLIC",
      "postedAt": "2024-01-15T10:30:00",
      "isCommentVisible": true,
      "isLikeVisible": false,
      "commentCount": 5,
      "media": [...],
      "user": {...}
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {...}
  },
  "totalElements": 100,
  "totalPages": 10,
  "last": false,
  "first": true,
  "numberOfElements": 10,
  "empty": false
}
```

## Example Requests

### Basic Filtering

```
GET /posts/filter?captions=hello&status=1&page=0&size=5
```

### Advanced Filtering with Pagination

```
GET /posts/filter?audience=PUBLIC&isCommentVisible=true&commentCount=5&commentCountOperator=>&page=1&size=20
```

### Date Filtering

```
GET /posts/filter?postedAt=2024-01-15&page=0&size=10
```

## Notes

- **Pagination**: The API now properly supports pagination with `LIMIT` and `OFFSET` clauses in the SQL query
- **Default Values**: If no pagination parameters are provided, defaults to page 0 with 10 items
- **Filter Combination**: All filters can be used individually or in combination
- **Case Insensitive**: Caption filtering is case-insensitive
- **Null Handling**: All filter parameters are optional and will be ignored if null

## Recent Fixes

- ✅ **Fixed Pagination Issue**: Added `LIMIT :#{#pageable.pageSize} OFFSET :#{#pageable.offset}` to the native SQL query
- ✅ **PostgreSQL Compatibility**: Fixed `bytea` type issue by using `CAST(p.captions AS TEXT)`
- ✅ **Proper Type Handling**: Ensured all database fields are properly typed for PostgreSQL
