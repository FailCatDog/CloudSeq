export const extractHeadingFlatList = (doc) => {
  const list = []
  doc.descendants((node, pos) => {
    if (node.type.name !== 'heading') return
    list.push({
      id: `h-${pos}-${node.attrs.level}`,
      level: node.attrs.level,
      text: node.textContent.trim() || '无标题',
      pos,
    })
  })
  return list
}

export const buildHeadingTree = (flatList) => {
  const root = []
  const stack = [{ level: 0, children: root }]

  for (const item of flatList) {
    const node = { ...item, children: [] }
    while (stack.length > 1 && stack[stack.length - 1].level >= item.level) {
      stack.pop()
    }
    stack[stack.length - 1].children.push(node)
    stack.push({ level: item.level, children: node.children })
  }

  return root
}

export const findActiveHeadingId = (flatList, cursorPos) => {
  if (!flatList.length) return null
  let active = flatList[0].id
  for (const item of flatList) {
    if (item.pos <= cursorPos) active = item.id
    else break
  }
  return active
}
