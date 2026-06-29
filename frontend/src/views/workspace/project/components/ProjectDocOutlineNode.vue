<script setup>
import ProjectDocOutlineNode from './ProjectDocOutlineNode.vue'

defineProps({
  item: {
    type: Object,
    required: true,
  },
  activeId: {
    type: String,
    default: null,
  },
  collapsedIds: {
    type: Object,
    required: true,
  },
})

const emit = defineEmits(['select', 'toggle'])

const isCollapsed = (id, collapsedIds) => collapsedIds.has(id)
</script>

<template>
  <li class="ps-outline-node">
    <div
      class="ps-outline-item"
      :class="{
        'is-active': activeId === item.id,
        [`ps-outline-item--h${item.level}`]: true,
      }"
      :style="{ paddingLeft: `${8 + (item.level - 1) * 14}px` }"
    >
      <button
        v-if="item.children.length"
        type="button"
        class="ps-outline-item__toggle"
        :class="{ 'is-collapsed': isCollapsed(item.id, collapsedIds) }"
        :aria-expanded="!isCollapsed(item.id, collapsedIds)"
        :aria-label="isCollapsed(item.id, collapsedIds) ? '展开' : '折叠'"
        @click.stop="emit('toggle', item.id)"
      >
        <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
          <polyline points="9 18 15 12 9 6" />
        </svg>
      </button>
      <span v-else class="ps-outline-item__toggle-placeholder" aria-hidden="true" />

      <button
        type="button"
        class="ps-outline-item__label"
        :title="item.text"
        @click="emit('select', item)"
      >
        {{ item.text }}
      </button>
    </div>

    <ul
      v-if="item.children.length && !isCollapsed(item.id, collapsedIds)"
      class="ps-outline-children"
    >
      <ProjectDocOutlineNode
        v-for="child in item.children"
        :key="child.id"
        :item="child"
        :active-id="activeId"
        :collapsed-ids="collapsedIds"
        @select="emit('select', $event)"
        @toggle="emit('toggle', $event)"
      />
    </ul>
  </li>
</template>
