import { ref, watch } from 'vue'
import {
  createDocumentCommentApi,
  deleteDocumentCommentApi,
  listDocumentCommentsApi,
} from '@/api/documentComment'
import { sortDocumentComments } from '@/views/workspace/project/script/projectDocComment.js'

export function useDocumentComments(getNodeId, getReady = () => true) {
  const comments = ref([])
  const loading = ref(false)
  const errorMessage = ref('')
  const submitting = ref(false)

  const resolveNodeId = () => {
    const value = typeof getNodeId === 'function' ? getNodeId() : getNodeId
    return value == null ? null : value
  }

  const loadComments = async () => {
    const nodeId = resolveNodeId()
    if (nodeId == null) {
      comments.value = []
      errorMessage.value = ''
      return
    }

    loading.value = true
    errorMessage.value = ''
    try {
      const list = await listDocumentCommentsApi(nodeId)
      comments.value = sortDocumentComments(list)
    } catch (error) {
      comments.value = []
      errorMessage.value = error?.message || '加载评论失败'
    } finally {
      loading.value = false
    }
  }

  const addComment = async ({ anchorPos, quoteText, content }) => {
    const nodeId = resolveNodeId()
    if (nodeId == null) throw new Error('文档未加载')

    submitting.value = true
    errorMessage.value = ''
    try {
      const created = await createDocumentCommentApi(nodeId, {
        anchorPos,
        quoteText: quoteText || null,
        content,
      })
      comments.value = sortDocumentComments([...comments.value, created])
      return created
    } catch (error) {
      errorMessage.value = error?.message || '发表评论失败'
      throw error
    } finally {
      submitting.value = false
    }
  }

  const removeComment = async (commentId) => {
    submitting.value = true
    errorMessage.value = ''
    try {
      await deleteDocumentCommentApi(commentId)
      comments.value = comments.value.filter((item) => String(item.id) !== String(commentId))
    } catch (error) {
      errorMessage.value = error?.message || '删除评论失败'
      throw error
    } finally {
      submitting.value = false
    }
  }

  watch(
    () => [resolveNodeId(), typeof getReady === 'function' ? getReady() : getReady],
    ([nodeId, ready]) => {
      if (nodeId == null || !ready) {
        comments.value = []
        errorMessage.value = ''
        return
      }
      void loadComments()
    },
    { immediate: true },
  )

  return {
    comments,
    loading,
    errorMessage,
    submitting,
    loadComments,
    addComment,
    removeComment,
  }
}
