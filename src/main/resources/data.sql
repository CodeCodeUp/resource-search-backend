-- 插入测试菜单数据
INSERT INTO menu (menu, name, level) VALUES 
('main', '主菜单', 1),
('education', '教育资源', 1),
('entertainment', '娱乐资源', 1),
('tools', '工具资源', 1),
('university', '大学教育', 2),
('primary', '基础教育', 2),
('exam', '考试资源', 2),
('cet', '四六级考试', 3),
('postgraduate', '研究生考试', 3),
('toefl', '托福考试', 3);

-- 插入测试资源数据
INSERT INTO resource (name, content, url, pig, level, type) VALUES 
('2025四六级答案', '2025年大学英语四六级考试答案和解析，包含听力、阅读、写作、翻译等各部分详细答案', 'https://example.com/cet-answers-2025', 'exam', 1, 'document'),
('四六级备考指南', '大学英语四六级考试备考策略，包括词汇、语法、听力、阅读等各项技能提升方法', 'https://example.com/cet-guide', 'study', 1, 'guide'),
('英语四级真题集', '历年英语四级考试真题集合，包含2020-2024年所有真题及答案解析', 'https://example.com/cet4-papers', 'exam', 2, 'document'),
('英语六级真题集', '历年英语六级考试真题集合，包含2020-2024年所有真题及答案解析', 'https://example.com/cet6-papers', 'exam', 2, 'document'),
('四六级词汇大全', '大学英语四六级考试必备词汇表，包含4000+核心词汇及例句', 'https://example.com/cet-vocabulary', 'vocab', 1, 'reference'),
('听力训练材料', '四六级听力专项训练材料，包含各种题型的练习和技巧讲解', 'https://example.com/listening-practice', 'practice', 2, 'audio'),
('写作模板大全', '四六级写作万能模板，包含议论文、说明文、应用文等各类写作模板', 'https://example.com/writing-templates', 'template', 1, 'document'),
('翻译技巧指南', '四六级翻译部分应试技巧，包含中英文翻译的常见句型和表达方式', 'https://example.com/translation-guide', 'skill', 2, 'guide'),
('阅读理解技巧', '四六级阅读理解解题技巧，包含快速阅读、仔细阅读等各种题型的应对策略', 'https://example.com/reading-skills', 'skill', 1, 'guide'),
('考试时间安排', '四六级考试时间分配建议，帮助考生合理安排各部分答题时间', 'https://example.com/time-management', 'strategy', 1, 'guide');
