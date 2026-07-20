/** 将菜单树扁平化为 n-tree 选项 */
export const flattenMenuTreeOptions = (nodes, acc = [], depth = 0) => {
  for (const node of nodes || []) {
    acc.push({
      key: node.id,
      label: `${'—'.repeat(depth)} ${node.menuName}`.trim(),
      raw: node,
    })
    if (node.children?.length) {
      flattenMenuTreeOptions(node.children, acc, depth + 1)
    }
  }
  return acc
}

/** 收集树中所有节点 id */
export const collectMenuIds = (nodes, acc = []) => {
  for (const node of nodes || []) {
    acc.push(node.id)
    if (node.children?.length) collectMenuIds(node.children, acc)
  }
  return acc
}

/** 转为 n-tree 数据（含 checkbox） */
export const toMenuTreeData = (nodes) => (nodes || []).map((node) => ({
  key: node.id,
  label: node.menuName,
  raw: node,
  children: node.children?.length ? toMenuTreeData(node.children) : undefined,
}))
