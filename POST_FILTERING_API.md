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
| `isLikeVisible`        | Boolean | No       | Filter posts by like visibility (default: false)                                                        | `false`                               |
| `commentCount`         | Long    | No       | Filter by comment count comparison                                                                      | `5`                                   |
| `commentCountOperator` | String  | No       | Comparison operator for comment count (default: "=")<br/>Supported: `>`, `<`, `=`, `>=`, `<=`           | `>`                                   |

### Pagination Parameters

| Parameter | Type    | Required | Default | Description              |
| --------- | ------- | -------- | ------- | ------------------------ |
| `page`    | Integer | No       | 0       | Page number (0-based)    |
| `size`    | Integer | No       | 10      | Number of items per page |

## Response Format

```json
{
  "posts": [
    {
      "id": "post-id",
      "captions": "Post content",
      "status": 1,
      "audience": "PUBLIC",
      "user": {
        "id": "user-id",
        "firstName": "John",
        "lastName": "Doe",
        "username": "johndoe"
      },
      "postedAt": "2024-01-15T10:30:00",
      "isCommentVisible": true,
      "isLikeVisible": false,
      "media": [],
      "hashtags": [],
      "commentCount": 5
    }
  ],
  "hasNext": true,
  "page": 0
}
```

## Usage Examples

### Filter by captions

```
GET /posts/filter?captions=hello&page=0&size=10
```

### Filter by status and audience

```
GET /posts/filter?status=1&audience=PUBLIC&page=0&size=10
```

### Filter by date range

```
GET /posts/filter?postedAt=2024-01-15&page=0&size=10
```

### Filter by comment count

```
GET /posts/filter?commentCount=5&commentCountOperator=>&page=0&size=10
```

### Combined filters

```
GET /posts/filter?captions=hello&status=1&audience=PUBLIC&isCommentVisible=true&commentCount=5&commentCountOperator=>=&page=0&size=10
```

## Notes

- All filter parameters are optional
- When a parameter is not provided, it won't be applied to the filter
- The response is paginated and ordered by `postedAt` in descending order
- Hashtag filtering is not implemented in this version (parameter is ignored)
- Date filtering supports both full datetime and date-only formats
- Comment count filtering uses SQL aggregation with HAVING clause
