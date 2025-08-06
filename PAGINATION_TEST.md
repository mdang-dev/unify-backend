# Pagination Test Guide

## Issue Identified and Fixed

The pagination issue was caused by the controller returning a `PostFeedResponse` instead of the full `Page<PostDto>` object. This was losing the detailed pagination information.

## Changes Made

1. **Controller Response Type**: Changed from `ResponseEntity<PostFeedResponse>` to `ResponseEntity<Page<PostDto>>`
2. **Repository**: Already had correct `LIMIT :#{#pageable.pageSize} OFFSET :#{#pageable.offset}` clauses
3. **Service**: Already properly handling pagination parameters

## How to Test Pagination

### Test URLs:

1. **Basic Pagination Test**:

   ```
   GET /posts/filter?page=0&size=5
   ```

2. **Second Page Test**:

   ```
   GET /posts/filter?page=1&size=5
   ```

3. **With Filters**:

   ```
   GET /posts/filter?status=1&page=0&size=10
   ```

4. **Advanced Filtering with Pagination**:
   ```
   GET /posts/filter?audience=PUBLIC&isCommentVisible=true&page=2&size=20
   ```

## Expected Response Format

The response should now include full pagination information:

```json
{
  "content": [
    {
      "id": "post-id-1",
      "captions": "Post content 1",
      "status": 1,
      "audience": "PUBLIC",
      "postedAt": "2024-01-15T10:30:00",
      "isCommentVisible": true,
      "isLikeVisible": false
    },
    {
      "id": "post-id-2",
      "captions": "Post content 2",
      "status": 1,
      "audience": "PUBLIC",
      "postedAt": "2024-01-15T09:30:00",
      "isCommentVisible": true,
      "isLikeVisible": false
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 5,
    "sort": {
      "empty": true,
      "sorted": false,
      "unsorted": true
    }
  },
  "totalElements": 100,
  "totalPages": 20,
  "last": false,
  "first": true,
  "numberOfElements": 5,
  "empty": false
}
```

## Key Pagination Fields

- **`content`**: Array of posts for the current page
- **`pageable.pageNumber`**: Current page number (0-based)
- **`pageable.pageSize`**: Number of items per page
- **`totalElements`**: Total number of posts matching the filter
- **`totalPages`**: Total number of pages
- **`last`**: Whether this is the last page
- **`first`**: Whether this is the first page
- **`numberOfElements`**: Number of elements on this page
- **`empty`**: Whether the page is empty

## Verification Steps

1. **Test Page 0**: Should return first 5 items
2. **Test Page 1**: Should return items 6-10 (different from page 0)
3. **Test Page 2**: Should return items 11-15 (different from page 0 and 1)
4. **Check `totalElements`**: Should be consistent across all pages
5. **Check `last` field**: Should be `true` only on the last page

## Common Issues to Check

1. **Same results on different pages**: Indicates pagination not working
2. **Missing pagination fields**: Indicates response format issue
3. **Incorrect `totalElements`**: Indicates count query issue
4. **Empty `content` on valid pages**: Indicates data or filter issue

## Debugging

If pagination still doesn't work:

1. Check the SQL logs to see if `LIMIT` and `OFFSET` are being applied
2. Verify the `page` and `size` parameters are being passed correctly
3. Check if the database has enough data to test pagination
4. Verify the response format includes all pagination fields
