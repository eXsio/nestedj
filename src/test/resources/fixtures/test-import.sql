insert into nested_nodes(id, tree_left, tree_level, tree_right, node_name, parent_id, discriminator) values(1000,1,0,16,'a',null, 'tree_1');
insert into nested_nodes(id, tree_left, tree_level, tree_right, node_name, parent_id, discriminator) values(2000,2,1,7,'b',1000, 'tree_1');
insert into nested_nodes(id, tree_left, tree_level, tree_right, node_name, parent_id, discriminator) values(3000,8,1,15,'c',1000, 'tree_1');
insert into nested_nodes(id, tree_left, tree_level, tree_right, node_name, parent_id, discriminator) values(4000,3,2,4,'d',2000, 'tree_1');
insert into nested_nodes(id, tree_left, tree_level, tree_right, node_name, parent_id, discriminator) values(5000,5,2,6,'e',2000, 'tree_1');
insert into nested_nodes(id, tree_left, tree_level, tree_right, node_name, parent_id, discriminator) values(6000,9,2,10,'f',3000, 'tree_1');
insert into nested_nodes(id, tree_left, tree_level, tree_right, node_name, parent_id, discriminator) values(7000,11,2,14,'g',3000, 'tree_1');
insert into nested_nodes(id, tree_left, tree_level, tree_right, node_name, parent_id, discriminator) values(8000,12,3,13,'h',7000, 'tree_1');

insert into nested_nodes(id, tree_left, tree_level, tree_right, node_name, parent_id, discriminator) values(9000,1,0,16,'a2',null, 'tree_2');
insert into nested_nodes(id, tree_left, tree_level, tree_right, node_name, parent_id, discriminator) values(10000,2,1,7,'b2',9000, 'tree_2');
insert into nested_nodes(id, tree_left, tree_level, tree_right, node_name, parent_id, discriminator) values(11000,8,1,15,'c2',9000, 'tree_2');
insert into nested_nodes(id, tree_left, tree_level, tree_right, node_name, parent_id, discriminator) values(12000,3,2,4,'d2',10000, 'tree_2');
insert into nested_nodes(id, tree_left, tree_level, tree_right, node_name, parent_id, discriminator) values(13000,5,2,6,'e2',10000, 'tree_2');
insert into nested_nodes(id, tree_left, tree_level, tree_right, node_name, parent_id, discriminator) values(14000,9,2,10,'f2',11000, 'tree_2');
insert into nested_nodes(id, tree_left, tree_level, tree_right, node_name, parent_id, discriminator) values(15000,11,2,14,'g2',11000, 'tree_2');
insert into nested_nodes(id, tree_left, tree_level, tree_right, node_name, parent_id, discriminator) values(16000,12,3,13,'h2',15000, 'tree_2');
