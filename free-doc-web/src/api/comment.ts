import { get, post, del } from '@/utils/request'
import type { Comment, CommentCreateRequest } from '@/types'

export function createComment(data: CommentCreateRequest): Promise<Comment> {
  return post('/comment', data)
}

export function deleteComment(commentId: string): Promise<void> {
  return del(`/comment/${commentId}`)
}

export function getDocComments(docId: string): Promise<Comment[]> {
  return get(`/comment/doc/${docId}`)
}
