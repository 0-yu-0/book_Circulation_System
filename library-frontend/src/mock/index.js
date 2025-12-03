import Mock from 'mockjs';
import { mockBooks, mockReaders, mockBorrowRecords } from './data';

// Mock setup
Mock.setup({
  timeout: '200-600'
});

// Auth APIs
Mock.mock(/\/api\/auth\/login/, 'post', (options) => {
  return {
    code: 0,
    message: 'ok',
    data: {
      token: 'mock-token-' + Date.now(),
      user: {
        id: 1,
        name: '管理员',
        role: 'admin'
      }
    }
  };
});

Mock.mock(/\/api\/auth\/logout/, 'post', {
  code: 0,
  message: 'ok'
});

// Book APIs
Mock.mock(/\/api\/books/, 'get', (options) => {
  const params = new URLSearchParams(options.url.split('?')[1]);
  const page = parseInt(params.get('page')) || 1;
  const size = parseInt(params.get('size')) || 20;
  const search = params.get('search') || '';
  
  let filteredBooks = mockBooks;
  if (search) {
    filteredBooks = mockBooks.filter(book => 
      book.title.includes(search) || 
      book.author.includes(search) || 
      book.isbn.includes(search)
    );
  }
  
  const start = (page - 1) * size;
  const end = start + size;
  const items = filteredBooks.slice(start, end);
  
  return {
    code: 0,
    data: {
      items,
      total: filteredBooks.length
    }
  };
});

Mock.mock(/\/api\/books\/\d+/, 'get', (options) => {
  const id = options.url.match(/\/api\/books\/(\d+)/)[1];
  const book = mockBooks.find(b => b.id === id);
  if (book) {
    return {
      code: 0,
      data: book
    };
  } else {
    return {
      code: 404,
      message: '图书不存在'
    };
  }
});

Mock.mock(/\/api\/books/, 'post', (options) => {
  const data = JSON.parse(options.body);
  const newBook = {
    ...data,
    id: String(mockBooks.length + 1),
    totalCopies: data.totalCopies || 1,
    availableCopies: data.availableCopies || 1
  };
  mockBooks.push(newBook);
  return {
    code: 0,
    data: newBook
  };
});

Mock.mock(/\/api\/books\/\d+/, 'put', (options) => {
  const id = options.url.match(/\/api\/books\/(\d+)/)[1];
  const data = JSON.parse(options.body);
  const index = mockBooks.findIndex(b => b.id === id);
  if (index >= 0) {
    mockBooks[index] = { ...mockBooks[index], ...data };
    return {
      code: 0,
      data: mockBooks[index]
    };
  } else {
    return {
      code: 404,
      message: '图书不存在'
    };
  }
});

Mock.mock(/\/api\/books\/\d+/, 'delete', (options) => {
  const id = options.url.match(/\/api\/books\/(\d+)/)[1];
  const index = mockBooks.findIndex(b => b.id === id);
  if (index >= 0) {
    mockBooks.splice(index, 1);
    return {
      code: 0,
      message: '删除成功'
    };
  } else {
    return {
      code: 404,
      message: '图书不存在'
    };
  }
});

// Reader APIs
Mock.mock(/\/api\/readers/, 'get', (options) => {
  const params = new URLSearchParams(options.url.split('?')[1]);
  const page = parseInt(params.get('page')) || 1;
  const size = parseInt(params.get('size')) || 20;
  const search = params.get('search') || '';
  
  let filteredReaders = mockReaders;
  if (search) {
    filteredReaders = mockReaders.filter(reader => 
      reader.name.includes(search) || 
      reader.idNumber.includes(search)
    );
  }
  
  const start = (page - 1) * size;
  const end = start + size;
  const items = filteredReaders.slice(start, end);
  
  return {
    code: 0,
    data: {
      items,
      total: filteredReaders.length
    }
  };
});

Mock.mock(/\/api\/readers\/\d+/, 'get', (options) => {
  const id = options.url.match(/\/api\/readers\/(\d+)/)[1];
  const reader = mockReaders.find(r => r.id === id);
  if (reader) {
    return {
      code: 0,
      data: reader
    };
  } else {
    return {
      code: 404,
      message: '读者不存在'
    };
  }
});

Mock.mock(/\/api\/readers/, 'post', (options) => {
  const data = JSON.parse(options.body);
  const newReader = {
    ...data,
    id: String(1000 + mockReaders.length + 1),
    currentBorrowCount: 0
  };
  mockReaders.push(newReader);
  return {
    code: 0,
    data: newReader
  };
});

Mock.mock(/\/api\/readers\/\d+/, 'put', (options) => {
  const id = options.url.match(/\/api\/readers\/(\d+)/)[1];
  const data = JSON.parse(options.body);
  const index = mockReaders.findIndex(r => r.id === id);
  if (index >= 0) {
    mockReaders[index] = { ...mockReaders[index], ...data };
    return {
      code: 0,
      data: mockReaders[index]
    };
  } else {
    return {
      code: 404,
      message: '读者不存在'
    };
  }
});

Mock.mock(/\/api\/readers\/\d+/, 'delete', (options) => {
  const id = options.url.match(/\/api\/readers\/(\d+)/)[1];
  const index = mockReaders.findIndex(r => r.id === id);
  if (index >= 0) {
    mockReaders.splice(index, 1);
    return {
      code: 0,
      message: '删除成功'
    };
  } else {
    return {
      code: 404,
      message: '读者不存在'
    };
  }
});

// Add missing API for getting reader by card number
Mock.mock(/\/api\/readers\/byCard\/.+/, 'get', (options) => {
  // Extract card number from URL (handles both numeric and alphanumeric card numbers)
  const match = options.url.match(/\/api\/readers\/byCard\/(.+)/);
  if (!match) {
    return {
      code: 400,
      message: '无效的请求'
    };
  }
  
  const cardNumber = match[1];
  const reader = mockReaders.find(r => r.idNumber === cardNumber);
  if (reader) {
    return {
      code: 0,
      data: reader
    };
  } else {
    return {
      code: 404,
      message: '读者不存在'
    };
  }
});

// Borrow APIs
Mock.mock(/\/api\/borrow/, 'get', (options) => {
  const params = new URLSearchParams(options.url.split('?')[1]);
  const readerId = params.get('readerId');
  const status = params.get('status');
  
  let filteredRecords = mockBorrowRecords;
  if (readerId) {
    filteredRecords = filteredRecords.filter(r => r.readerId === readerId);
  }
  if (status) {
    const statusMap = { borrowed: 0, returned: 1, overdue: 2 };
    filteredRecords = filteredRecords.filter(r => r.borrowStates === statusMap[status]);
  }
  
  // Add book and reader details to each record
  const recordsWithDetails = filteredRecords.map(record => {
    const book = mockBooks.find(b => b.id === record.bookId);
    const reader = mockReaders.find(r => r.id === record.readerId);
    
    return {
      ...record,
      bookTitle: book ? book.title : 'Unknown Book',
      bookAuthor: book ? book.author : 'Unknown Author',
      readerName: reader ? reader.name : 'Unknown Reader',
      readerIdNumber: reader ? reader.idNumber : 'Unknown ID'
    };
  });
  
  return {
    code: 0,
    data: {
      items: recordsWithDetails
    }
  };
});

Mock.mock(/\/api\/borrow/, 'post', (options) => {
  const data = JSON.parse(options.body);
  const borrowId = String(20230000 + mockBorrowRecords.length + 1);
  
  // Create borrow records
  const records = data.books.map(book => ({
    borrowId,
    bookId: book.bookId,
    readerId: data.readerId,
    borrowDate: data.borrowDate,
    dueDate: data.dueDate,
    borrowStates: 0 // 在借
  }));
  
  mockBorrowRecords.push(...records);
  
  // Update book availability
  data.books.forEach(book => {
    const bookObj = mockBooks.find(b => b.id === book.bookId);
    if (bookObj) {
      bookObj.availableCopies -= book.count;
    }
  });
  
  // Update reader borrow count
  const reader = mockReaders.find(r => r.id === data.readerId);
  if (reader) {
    reader.currentBorrowCount += data.books.length;
  }
  
  return {
    code: 0,
    data: {
      borrowId,
      receipt: {
        borrowId,
        borrowDate: data.borrowDate,
        dueDate: data.dueDate,
        items: records.map(record => {
          const book = mockBooks.find(b => b.id === record.bookId);
          return {
            bookTitle: book ? book.title : '',
            bookAuthor: book ? book.author : ''
          };
        })
      }
    }
  };
});

Mock.mock(/\/api\/return/, 'post', (options) => {
  const data = JSON.parse(options.body);
  
  // Process returns
  const returned = [];
  let totalFine = 0;
  
  data.borrowIds.forEach(borrowId => {
    const record = mockBorrowRecords.find(r => r.borrowId === borrowId);
    if (record && record.borrowStates === 0) { // 在借状态
      record.borrowStates = 1; // 更新为已还
      
      // Calculate fine if overdue
      const dueDate = new Date(record.dueDate);
      const returnDate = new Date(data.returnDate);
      const diffTime = returnDate - dueDate;
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
      
      let fine = 0;
      if (diffDays > 0) {
        fine = diffDays * 0.5; // 每天0.5元罚款
        record.borrowStates = 2; // 逾期
      }
      
      returned.push({
        borrowId: record.borrowId,
        bookId: record.bookId,
        fine
      });
      
      totalFine += fine;
      
      // Update book availability
      const book = mockBooks.find(b => b.id === record.bookId);
      if (book) {
        book.availableCopies += 1;
      }
      
      // Update reader borrow count
      const reader = mockReaders.find(r => r.id === record.readerId);
      if (reader && reader.currentBorrowCount > 0) {
        reader.currentBorrowCount -= 1;
      }
    }
  });
  
  return {
    code: 0,
    data: {
      returned,
      fines: totalFine > 0 ? [{ amount: totalFine }] : []
    }
  };
});

// Add missing API for getting borrow records
Mock.mock(/\/api\/borrow\/record/, 'get', (options) => {
  const params = new URLSearchParams(options.url.split('?')[1]);
  const readerId = params.get('readerId');
  const status = params.get('status');
  
  let filteredRecords = mockBorrowRecords;
  if (readerId) {
    filteredRecords = filteredRecords.filter(r => r.readerId === readerId);
  }
  if (status) {
    const statusMap = { borrowed: 0, returned: 1, overdue: 2 };
    filteredRecords = filteredRecords.filter(r => r.borrowStates === statusMap[status]);
  }
  
  // Add book and reader details to each record
  const recordsWithDetails = filteredRecords.map(record => {
    const book = mockBooks.find(b => b.id === record.bookId);
    const reader = mockReaders.find(r => r.id === record.readerId);
    
    return {
      ...record,
      bookTitle: book ? book.title : 'Unknown Book',
      bookAuthor: book ? book.author : 'Unknown Author',
      readerName: reader ? reader.name : 'Unknown Reader',
      readerIdNumber: reader ? reader.idNumber : 'Unknown ID'
    };
  });
  
  return {
    code: 0,
    data: {
      items: recordsWithDetails
    }
  };
});

// Statistics APIs
Mock.mock(/\/api\/statistics\/overview/, 'get', {
  code: 0,
  data: {
    totalBooks: mockBooks.length,
    totalReaders: mockReaders.length,
    borrowedCount: mockBorrowRecords.filter(r => r.borrowStates === 0).length, // 在借数量
    todayBorrows: mockBorrowRecords.filter(r => r.borrowStates === 0 && r.borrowDate === new Date().toISOString().split('T')[0]).length || 0, // 今日借书次数
    todayReturns: mockBorrowRecords.filter(r => r.borrowStates === 1 && r.returnDate && r.returnDate === new Date().toISOString().split('T')[0]).length || 0, // 今日还书次数
    overdueCount: mockBorrowRecords.filter(r => r.borrowStates === 2).length // 逾期未还数量
  }
});

Mock.mock(/\/api\/statistics\/popular-books/, 'get', (options) => {
  const url = new URL(options.url, 'http://localhost');
  const params = url.searchParams;
  const top = parseInt(params.get('top')) || 10;
  
  // 生成更真实的热门图书数据
  const popularBooks = mockBooks
    .map(book => {
      // 根据图书的可用副本数和总副本数计算受欢迎程度
      const popularityFactor = book.totalCopies > 0 ? (book.totalCopies - book.availableCopies) / book.totalCopies : 0;
      // 添加一些随机因素
      const randomFactor = Math.random() * 0.5 + 0.5;
      const borrowCount = Math.floor(popularityFactor * 20 * randomFactor) + 1;
      
      return {
        ...book,
        borrowCount: borrowCount
      };
    })
    .sort((a, b) => b.borrowCount - a.borrowCount)
    .slice(0, top)
    .map(book => ({
      title: book.title,
      bookTitle: book.title,
      borrowCount: book.borrowCount,
      author: book.author,
      category: book.category
    }));
  
  return {
    code: 0,
    data: popularBooks
  };
});

Mock.mock(/\/api\/statistics\/overdue-books/, 'get', (options) => {
  const params = new URLSearchParams(options.url.split('?')[1]);
  const page = parseInt(params.get('page')) || 1;
  const size = parseInt(params.get('size')) || 10;
  
  const overdueRecords = mockBorrowRecords
    .filter(r => r.borrowStates === 2); // 逾期状态
  
  const start = (page - 1) * size;
  const end = start + size;
  const items = overdueRecords.slice(start, end);
  
  const result = items.map(record => {
    const book = mockBooks.find(b => b.id === record.bookId);
    const reader = mockReaders.find(r => r.id === record.readerId);
    
    // 计算逾期天数
    const dueDate = new Date(record.dueDate);
    const today = new Date();
    const diffTime = today - dueDate;
    const overdueDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    
    return {
      borrowId: record.borrowId,
      bookTitle: book ? book.title : 'Unknown Book',
      readerName: reader ? reader.name : 'Unknown Reader',
      dueDate: record.dueDate,
      overdueDays: overdueDays > 0 ? overdueDays : 1,
      category: book ? book.category : 'Unknown'
    };
  });
  
  return {
    code: 0,
    data: {
      items: result,
      total: overdueRecords.length
    }
  };
});

export default Mock;