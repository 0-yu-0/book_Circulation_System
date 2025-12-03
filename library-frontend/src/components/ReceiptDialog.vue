<template>
  <el-dialog title="借阅凭证" :visible.sync="visible" width="600px" @close="$emit('update:visible', false)">
    <div id="print-ticket" class="ticket" v-if="receipt">
      <div class="title">图书借阅凭证</div>
      <div class="meta">借阅时间：{{ receipt.borrowDate }}</div>
      <div class="meta">应还时间：{{ receipt.dueDate }}</div>
      <div class="meta">借阅编号：{{ receipt.borrowId }}</div>
      <div class="separator"></div>
      <div class="book-row" v-for="(item, index) in receipt.items" :key="index">
        <span class="left">{{ item.bookTitle }} - {{ item.bookAuthor }}</span>
      </div>
      <div class="separator"></div>
      <div class="footer">请妥善保管此凭证，按时归还图书</div>
    </div>
    <template #footer>
      <el-button @click="$emit('update:visible', false)">关闭</el-button>
      <el-button type="primary" @click="print">打印</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
const props = defineProps({ visible: Boolean, receipt: Object })
const emit = defineEmits(['update:visible'])

function print(){
  window.print()
}
</script>

<!-- styles are now in global src/styles/print-ticket.css -->
