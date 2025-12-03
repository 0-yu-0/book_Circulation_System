// Mock data for the library system
export const mockBooks = [
  {
    id: '1',
    title: 'Java核心技术',
    author: 'Cay S. Horstmann',
    publisher: '机械工业出版社',
    publishDate: '2022-01-01',
    category: '计算机',
    location: 'A区001架01层',
    totalCopies: 10,
    availableCopies: 5,
    isbn: '9787111601157',
    cover: ''
  },
  {
    id: '2',
    title: 'JavaScript高级程序设计',
    author: 'Matt Frisbie',
    publisher: '人民邮电出版社',
    publishDate: '2020-05-15',
    category: '计算机',
    location: 'A区002架03层',
    totalCopies: 8,
    availableCopies: 2,
    isbn: '9787115547345',
    cover: ''
  },
  {
    id: '3',
    title: '红楼梦',
    author: '曹雪芹',
    publisher: '人民文学出版社',
    publishDate: '2019-08-20',
    category: '文学',
    location: 'B区005架02层',
    totalCopies: 15,
    availableCopies: 10,
    isbn: '9787020002207',
    cover: ''
  },
  {
    id: '4',
    title: '西游记',
    author: '吴承恩',
    publisher: '人民文学出版社',
    publishDate: '2018-12-10',
    category: '文学',
    location: 'B区006架01层',
    totalCopies: 12,
    availableCopies: 7,
    isbn: '9787020002221',
    cover: ''
  },
  {
    id: '5',
    title: '数据结构与算法分析',
    author: 'Mark Allen Weiss',
    publisher: '机械工业出版社',
    publishDate: '2021-03-18',
    category: '计算机',
    location: 'A区003架04层',
    totalCopies: 6,
    availableCopies: 1,
    isbn: '9787111488877',
    cover: ''
  },
  {
    id: '6',
    title: 'Python编程：从入门到实践',
    author: 'Eric Matthes',
    publisher: '人民邮电出版社',
    publishDate: '2021-01-01',
    category: '计算机',
    location: 'A区004架02层',
    totalCopies: 12,
    availableCopies: 8,
    isbn: '9787115428028',
    cover: ''
  },
  {
    id: '7',
    title: '三国演义',
    author: '罗贯中',
    publisher: '人民文学出版社',
    publishDate: '2020-05-01',
    category: '文学',
    location: 'B区007架03层',
    totalCopies: 10,
    availableCopies: 3,
    isbn: '9787020002238',
    cover: ''
  },
  {
    id: '8',
    title: '活着',
    author: '余华',
    publisher: '作家出版社',
    publishDate: '2019-10-01',
    category: '文学',
    location: 'B区008架01层',
    totalCopies: 15,
    availableCopies: 0,
    isbn: '9787506365437',
    cover: ''
  },
  {
    id: '9',
    title: '深入理解计算机系统',
    author: 'Randal E. Bryant',
    publisher: '机械工业出版社',
    publishDate: '2022-03-15',
    category: '计算机',
    location: 'A区005架05层',
    totalCopies: 7,
    availableCopies: 2,
    isbn: '9787111544937',
    cover: ''
  },
  {
    id: '10',
    title: '百年孤独',
    author: '加西亚·马尔克斯',
    publisher: '南海出版公司',
    publishDate: '2021-07-01',
    category: '文学',
    location: 'B区009架02层',
    totalCopies: 9,
    availableCopies: 4,
    isbn: '9787544256497',
    cover: ''
  }
];

export const mockReaders = [
  {
    id: '1001',
    name: '张三',
    idType: '身份证',
    idNumber: '110101199003077856',
    phone: '13800138000',
    registerDate: '2023-01-15',
    status: '正常',
    borrowLimit: 5,
    currentBorrowCount: 3
  },
  {
    id: '1002',
    name: '李四',
    idType: '身份证',
    idNumber: '110101198512038914',
    phone: '13900139000',
    registerDate: '2023-02-20',
    status: '正常',
    borrowLimit: 5,
    currentBorrowCount: 1
  },
  {
    id: '1003',
    name: '王五',
    idType: '身份证',
    idNumber: '110101199208156732',
    phone: '13700137000',
    registerDate: '2023-03-10',
    status: '正常',
    borrowLimit: 5,
    currentBorrowCount: 0
  },
  {
    id: '1004',
    name: '赵六',
    idType: '学生证',
    idNumber: '2023001234',
    phone: '13600136000',
    registerDate: '2023-04-05',
    status: '正常',
    borrowLimit: 3,
    currentBorrowCount: 2
  },
  {
    id: '1005',
    name: '钱七',
    idType: '身份证',
    idNumber: '110101199106125432',
    phone: '13500135000',
    registerDate: '2023-05-12',
    status: '正常',
    borrowLimit: 5,
    currentBorrowCount: 0
  },
  {
    id: '1006',
    name: '孙八',
    idType: '学生证',
    idNumber: '2023005678',
    phone: '13400134000',
    registerDate: '2023-06-18',
    status: '正常',
    borrowLimit: 3,
    currentBorrowCount: 1
  },
  {
    id: '1007',
    name: '周九',
    idType: '身份证',
    idNumber: '110101198811223344',
    phone: '13300133000',
    registerDate: '2023-07-22',
    status: '挂失',
    borrowLimit: 5,
    currentBorrowCount: 2
  }
];

export const mockBorrowRecords = [
  {
    borrowId: '20230001',
    bookId: '1',
    readerId: '1001',
    borrowDate: '2023-10-01',
    dueDate: '2023-10-31',
    borrowStates: 0 // 在借
  },
  {
    borrowId: '20230002',
    bookId: '2',
    readerId: '1001',
    borrowDate: '2023-10-05',
    dueDate: '2023-11-04',
    borrowStates: 0 // 在借
  },
  {
    borrowId: '20230003',
    bookId: '3',
    readerId: '1001',
    borrowDate: '2023-09-20',
    dueDate: '2023-10-20',
    borrowStates: 2 // 逾期
  },
  {
    borrowId: '20230004',
    bookId: '4',
    readerId: '1002',
    borrowDate: '2023-10-10',
    dueDate: '2023-11-09',
    borrowStates: 0 // 在借
  },
  {
    borrowId: '20230005',
    bookId: '5',
    readerId: '1006',
    borrowDate: '2023-10-15',
    dueDate: '2023-11-14',
    borrowStates: 0 // 在借
  },
  {
    borrowId: '20230006',
    bookId: '7',
    readerId: '1005',
    borrowDate: '2023-09-25',
    dueDate: '2023-10-25',
    borrowStates: 2 // 逾期
  },
  {
    borrowId: '20230007',
    bookId: '9',
    readerId: '1002',
    borrowDate: '2023-10-20',
    dueDate: '2023-11-19',
    borrowStates: 0 // 在借
  },
  {
    borrowId: '20230008',
    bookId: '1',
    readerId: '1005',
    borrowDate: '2023-10-18',
    dueDate: '2023-11-17',
    borrowStates: 1 // 已还
  }
];