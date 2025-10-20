-- 注意：该页面对应的前台目录为views/youcai文件夹下
-- 如果你想更改到其他目录，请修改sql中component字段对应的值


-- 主菜单
INSERT INTO sys_permission(id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_route, is_leaf, keep_alive, hidden, hide_tab, description, status, del_flag, rule_flag, create_by, create_time, update_by, update_time, internal_or_external)
VALUES ('176076575818201', NULL, '地块信息表', '/youcai/youcaiPlotsList', 'youcai/YoucaiPlotsList', NULL, NULL, 0, NULL, '1', 0.00, 0, NULL, 1, 0, 0, 0, 0, NULL, '1', 0, 0, 'admin', '2025-10-18 13:35:58', NULL, NULL, 0);

-- 新增
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176076575818202', '176076575818201', '添加地块信息表', NULL, NULL, 0, NULL, NULL, 2, 'youcai:youcai_plots:add', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2025-10-18 13:35:58', NULL, NULL, 0, 0, '1', 0);

-- 编辑
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176076575818203', '176076575818201', '编辑地块信息表', NULL, NULL, 0, NULL, NULL, 2, 'youcai:youcai_plots:edit', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2025-10-18 13:35:58', NULL, NULL, 0, 0, '1', 0);

-- 删除
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176076575818204', '176076575818201', '删除地块信息表', NULL, NULL, 0, NULL, NULL, 2, 'youcai:youcai_plots:delete', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2025-10-18 13:35:58', NULL, NULL, 0, 0, '1', 0);

-- 批量删除
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176076575818205', '176076575818201', '批量删除地块信息表', NULL, NULL, 0, NULL, NULL, 2, 'youcai:youcai_plots:deleteBatch', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2025-10-18 13:35:58', NULL, NULL, 0, 0, '1', 0);

-- 导出excel
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176076575818206', '176076575818201', '导出excel_地块信息表', NULL, NULL, 0, NULL, NULL, 2, 'youcai:youcai_plots:exportXls', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2025-10-18 13:35:58', NULL, NULL, 0, 0, '1', 0);

-- 导入excel
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176076575818207', '176076575818201', '导入excel_地块信息表', NULL, NULL, 0, NULL, NULL, 2, 'youcai:youcai_plots:importExcel', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2025-10-18 13:35:58', NULL, NULL, 0, 0, '1', 0);

-- 角色授权（以 admin 角色为例，role_id 可替换）
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176076575818208', 'f6817f48af4fb3af11b9e8bf182f618b', '176076575818201', NULL, '2025-10-18 13:35:58', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176076575818209', 'f6817f48af4fb3af11b9e8bf182f618b', '176076575818202', NULL, '2025-10-18 13:35:58', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176076575818210', 'f6817f48af4fb3af11b9e8bf182f618b', '176076575818203', NULL, '2025-10-18 13:35:58', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176076575818211', 'f6817f48af4fb3af11b9e8bf182f618b', '176076575818204', NULL, '2025-10-18 13:35:58', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176076575818212', 'f6817f48af4fb3af11b9e8bf182f618b', '176076575818205', NULL, '2025-10-18 13:35:58', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176076575818213', 'f6817f48af4fb3af11b9e8bf182f618b', '176076575818206', NULL, '2025-10-18 13:35:58', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176076575818214', 'f6817f48af4fb3af11b9e8bf182f618b', '176076575818207', NULL, '2025-10-18 13:35:58', '127.0.0.1');