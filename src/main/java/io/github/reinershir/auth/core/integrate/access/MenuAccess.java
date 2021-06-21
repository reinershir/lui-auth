package io.github.reinershir.auth.core.integrate.access;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.github.reinershir.auth.core.integrate.vo.MenuVO;
import io.github.reinershir.auth.core.model.Menu;

public class MenuAccess extends AbstractAccess<Menu>{

	JdbcTemplate jdbcTemplate;
	String tableName;
	String roleTableName;
	MenuRowMapper mapper = new MenuRowMapper();
	
	public MenuAccess(JdbcTemplate jdbcTemplate,String tableName,String roleTableName) {
		super(jdbcTemplate,tableName);
		this.jdbcTemplate=jdbcTemplate;
		this.tableName=tableName;
		this.roleTableName=roleTableName;
	}
	
	public List<Menu> qureyList(@Nullable Long parentId){
		List<Menu> list = new LinkedList<>();
		
		if(parentId==null) {
			list =  jdbcTemplate.query("SELECT * FROM "+tableName+" ORDER BY LEFT_VALUE ASC", mapper);
		}else {
			Menu parent = selectById(parentId);
			list = jdbcTemplate.query("SELECT * FROM "+tableName+" WHERE LEFT_VALUE>? AND RIGHT_VALUE < ? ORDER BY LEFT_VALUE", mapper,parent.getLeftValue(),parent.getRightValue());
		}
		return convertToTree(list);
	}
	
	/**
	 * @Title: convertToTree
	 * @Description:   将左右值列表转换为树形结构,(由于左右值结构数据查出来的是有顺序的，所以可以按left和right值判断层级关系)
	 * @author reinershir
	 * @date 2020年12月2日
	 * @param list
	 * @return
	 */
	private List<Menu> convertToTree(List<Menu> list ){
		List<Menu> resultList = new LinkedList<>();
		//记录上一个元素的菜单层级
		//Integer beforeLevel = -999;
		for (Menu menu : list) {
			//if(beforeLevel==menu.getLevel()-1||(beforeLevel==menu.getLevel()&&menu.getLevel()!=1)) {
			if(menu.getLevel()>1) {
				//根据层级关系装配菜单数据
				assemblingChildMenu(resultList,menu);
			}else {
				//如果是父菜单直接加进来
				resultList.add(menu);
			}
			//beforeLevel = menu.getLevel();
		}
		return resultList;
	}
	
	/**
	 * @Title: assemblingChildMenu
	 * @Description: 根据菜单层级装配为数形结构数据  
	 * @author reinershir
	 * @date 2020年12月2日
	 * @param resultList 已装配好的数据
	 * @param menu 要查找的数据
	 */
	private void assemblingChildMenu(List<Menu> resultList,Menu menu) {
		if(!CollectionUtils.isEmpty(resultList)&&menu!=null) {
			for (Menu parent : resultList) {
				//判断是否是该菜单的子节点
				if(menu.getLeftValue()>parent.getLeftValue()&&menu.getRightValue()<parent.getRightValue()) {
					//双重验证，再通过层级判断下
					if(menu.getLevel()-1==parent.getLevel()) {
						parent.getChildren().add(menu);
						break;
					}else {
						//如果层级不对，说明该节点在它子节点下面
						assemblingChildMenu(parent.getChildren(),menu);
					}
				}
			}
		}
	}
	
	public Long selectCount(@Nullable String menuName) {
		return super.selectCount(menuName, "NAME");
	}
	
	/**
	 * @Title: insertMenu
	 * @Description: 添加菜单数据
	 * @author reinershir
	 * @date 2020年12月1日
	 * @param menu 要添加的菜单数据对象
	 * @param parentId 指定要添加的父节点ID，即菜单会添加为该菜单ID的子菜单
	 * @return 1 = true
	 */
	@Transactional
	public int insertMenu(@Nonnull MenuVO menuVO,@Nullable Long parentId) {
		int result =-1;
		if(menuVO!=null) {
			Menu menu = new Menu();
			BeanUtils.copyProperties(menuVO,menu);
			KeyHolder holder = new GeneratedKeyHolder();
			//如果是添加初始节点
			if(parentId==null) {
				Menu first = getFirstNode();
				if(first==null) {
					//如果没有一级菜单，使用默认值
					menu.setLeftValue(1);
					menu.setRightValue(2);
					menu.setLevel(1);
				}else {
					//先更新左右节点值
					jdbcTemplate.update("UPDATE "+tableName+" SET LEFT_VALUE=LEFT_VALUE+2 WHERE LEFT_VALUE > ?",first.getRightValue());
					jdbcTemplate.update(" UPDATE "+tableName+" SET RIGHT_VALUE=RIGHT_VALUE+2 WHERE RIGHT_VALUE > ?",first.getRightValue());
					menu.setLeftValue(first.getRightValue()+1);
					menu.setRightValue(first.getRightValue()+2);
					menu.setLevel(first.getLevel());
				}
			}else {
				//如果是添加到指定节点下面
				Menu parent = selectById(parentId);
				//计算受影响节点
				jdbcTemplate.update("UPDATE "+tableName+" SET LEFT_VALUE=LEFT_VALUE+2 WHERE LEFT_VALUE >= ?",parent.getRightValue());
				jdbcTemplate.update("UPDATE "+tableName+" SET RIGHT_VALUE=RIGHT_VALUE+2 WHERE RIGHT_VALUE >= ?",parent.getRightValue());
				menu.setLeftValue(parent.getRightValue());
				menu.setRightValue(parent.getRightValue()+1);
				menu.setLevel(parent.getLevel()+1);
			}
			String sql = "INSERT INTO "+tableName+"(NAME,URL,ICON,PERMISSION_CODES,DESCRIPTION,PROPERTY,CREATE_DATE,LEFT_VALUE,RIGHT_VALUE,LEVEL) VALUES(?,?,?,?,?,?,?,?,?,?)";
			result = jdbcTemplate.update(new PreparedStatementCreator() {
				@Override
				public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
					PreparedStatement ps = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
					ps.setString(1, menu.getName());
					ps.setString(2, menu.getUrl());
					ps.setString(3, menu.getIcon());
					ps.setString(4, menu.getPermissionCodes());
					ps.setString(5, menu.getDescription());
					ps.setString(6, menu.getProperty());
					ps.setDate(7, new java.sql.Date(new Date().getTime()));
					ps.setInt(8, menu.getLeftValue());
					ps.setInt(9, menu.getRightValue());
					ps.setInt(10, menu.getLevel());
					return ps;
				}
			},holder);
			menu.setId(holder.getKey().longValue());
		}
		return result;
	}
	
	private Menu getFirstNode() {
		StringBuilder sql = new StringBuilder("SELECT * FROM ");
		sql.append(tableName);
		sql.append(" WHERE LEFT_VALUE = (SELECT MAX(M.LEFT_VALUE) FROM ");
		sql.append(tableName);
		sql.append(" M WHERE M.LEVEL = 1)");
		try {
			return jdbcTemplate.queryForObject(sql.toString(), mapper);
		} catch (DataAccessException e) {
			return null;
		}
	}
	
	public Menu selectById(Long id) {
		return super.selectById(id, mapper);
	}
	
	public List<Menu> selectByList(List<Long> ids){
		List<Menu> list = super.selectByList(ids, mapper);
		if(!CollectionUtils.isEmpty(list)) {
			//排序
			list.sort((m1,m2)-> m2.getLeftValue().compareTo(m1.getLeftValue()));
			return convertToTree(list);
		}
		return null;
		
	}
	
	@Transactional
	public int updateById(@Nonnull MenuVO menuVO) {
		if(menuVO!=null&&menuVO.getId()!=null) {
			Menu menu = new Menu();
			BeanUtils.copyProperties(menuVO,menu);
			int result =-1;
			result = jdbcTemplate.update("UPDATE "+tableName+" SET NAME=?,URL=?,ICON=?,DESCRIPTION=?,PROPERTY=?,PERMISSION_CODES=?,UPDATE_DATE=? WHERE ID = ?", new PreparedStatementSetter() {
				
				@Override
				public void setValues(PreparedStatement ps) throws SQLException {
					ps.setString(1, menu.getName());
					ps.setString(2,menu.getUrl());
					ps.setString(3, menu.getIcon());
					ps.setString(4, menu.getDescription());
					ps.setString(5, menu.getProperty());
					ps.setString(6, menu.getPermissionCodes());
					ps.setDate(7, new java.sql.Date(new Date().getTime()));
					ps.setLong(8, menu.getId());
				}
			});
			StringBuilder sql = new StringBuilder("UPDATE ");
			sql.append(roleTableName);
			sql.append("_PERMISSION SET PERMISSION_CODES = ? WHERE MENU_ID = ?");
			jdbcTemplate.update(sql.toString(),menu.getPermissionCodes(),menu.getId());
			return result;
		}
		return -1;
	}
	
	@Transactional
	public int deleteById(Long id) {
		int result =-1;
		Menu menu = selectById(id);
		if(menu!=null) {
			//删除节点
			result = jdbcTemplate.update("DELETE FROM "+tableName+" WHERE LEFT_VALUE >=? and RIGHT_VALUE <=?",menu.getLeftValue(),menu.getRightValue());
			//修改受影响的节点
			jdbcTemplate.update("update "+tableName+" set LEFT_VALUE=LEFT_VALUE-(? - ? + 1) where LEFT_VALUE > ?",menu.getRightValue(),menu.getLeftValue(),menu.getLeftValue());
			jdbcTemplate.update("update "+tableName+" set RIGHT_VALUE=RIGHT_VALUE-(? - ? + 1) where RIGHT_VALUE > ?",menu.getRightValue(),menu.getLeftValue(),menu.getRightValue());
			//删除角色关联的菜单
			jdbcTemplate.update("DELETE FROM "+roleTableName+"_PERMISSION WHERE MENU_ID = ?",id);
		}
		return result;
	}
	
	public int switchNode(Long id,Long anotherId) {
		StringBuilder sql = new StringBuilder("UPDATE ");
		sql.append(tableName);
		sql.append(" M,");
		sql.append(tableName);
		sql.append(" A");
		sql.append("SET M.LEFT_VALUE = A.LEFT_VALUE,A.LEFT_VALUE = M.LEFT_VALUE,M.RIGHT_VALUE = A.RIGHT_VALUE,A.RIGHT_VALUE = M.RIGHT_VALUE WHERE M.ID=? AND A.ID=? AND M.LEVEL=A.LEVEL");
		return jdbcTemplate.update(sql.toString(),id,anotherId);
	}
	
	/**
	 * @Title: moveNodeByParentAsLastChild
	 * @Description: 移动菜单到目标菜单下作为目标菜单的最后一个子菜单
	 * @author reinershir
	 * @date 2021年5月26日
	 * @param moveId 要移动的菜单ID
	 * @param targetId 目标父菜单ID
	 * @return
	 */
	@Transactional
	public int moveNodeByParentAsLastChild(Long moveId,Long targetId) {
		//LOCK TABLE
		this.lockTable();
		   
		Menu moveMenu = this.selectById(moveId);
		
		Menu targetMenu = this.selectById(targetId);
		
		Integer moveLeft = moveMenu.getLeftValue(); 
		
		Integer moveRight = moveMenu.getRightValue();
		////要移动菜单的范围，即被移动菜单及其子节点的左右值范围
		Integer moveDistance = moveRight - moveLeft+1; 
		
		Integer level = moveMenu.getLevel();
		
		Integer targetRight = targetMenu.getRightValue();
		
		Integer targetLevel = targetMenu.getLevel();
		
		
		if((targetMenu.getLeftValue()>=moveLeft&&targetRight<=moveRight)||(moveId==targetId)) {
			unlockTables();
			return -1;
		}
		Integer result = 0;
		//设置子节点的新值 
		StringBuilder sql = new StringBuilder("UPDATE ");
		sql.append(tableName);
		sql.append(" SET LEFT_VALUE = CASE WHEN LEFT_VALUE > :targetRight THEN LEFT_VALUE + :moveDistance ELSE LEFT_VALUE END ,");
		sql.append("RIGHT_VALUE = CASE WHEN RIGHT_VALUE >= :targetRight THEN RIGHT_VALUE + :moveDistance ELSE RIGHT_VALUE END ");
		sql.append("WHERE RIGHT_VALUE >= :targetRight");
		
		MapSqlParameterSource  params = new MapSqlParameterSource();
		params.addValue("moveLeft", moveLeft);
		params.addValue("moveRight", moveRight);
		params.addValue("targetRight", targetRight);
		params.addValue("moveDistance", moveDistance);
		
		NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		
		result+=namedParameterJdbcTemplate.update(sql.toString(),params);
		
		//再次查询 
		moveMenu = this.selectById(moveId);
		moveLeft = moveMenu.getLeftValue(); 
		moveRight = moveMenu.getRightValue();
		
		Integer newDistance = targetRight>=moveLeft?targetRight-moveLeft:moveLeft - targetRight;
		Integer newDistanceOperator = targetRight >=moveLeft?1:0;
		params.addValue("newDistanceOperator", newDistanceOperator);
		params.addValue("newDistance", newDistance);
		params.addValue("level", level);
		params.addValue("targetLevel", targetLevel);
		//重新塞入
		params.addValue("moveLeft", moveLeft);
		params.addValue("moveRight", moveRight);
		
		System.out.println("new Distance:"+newDistance+" level:"+level+" targetLevel:"+ targetLevel+" newDistanceOperator:"+newDistanceOperator);
		System.out.println("move left:"+moveLeft+" move right:"+moveRight);
		//移动节点
		sql = new StringBuilder("UPDATE ");
		sql.append(tableName);
		sql.append(" SET LEFT_VALUE = CASE WHEN :newDistanceOperator = 1 THEN LEFT_VALUE + :newDistance ELSE LEFT_VALUE - :newDistance END ,");
		sql.append("RIGHT_VALUE = CASE WHEN :newDistanceOperator = 1 THEN RIGHT_VALUE + :newDistance ELSE RIGHT_VALUE - :newDistance END ,");
		sql.append("LEVEL = LEVEL - :level +1 + :targetLevel WHERE RIGHT_VALUE <= :moveRight AND LEFT_VALUE >= :moveLeft");
	
		result += namedParameterJdbcTemplate.update(sql.toString(),params);
		sql = new StringBuilder("UPDATE ");
		sql.append(tableName);
		sql.append(" SET LEFT_VALUE = CASE WHEN LEFT_VALUE > :moveRight THEN LEFT_VALUE - :moveDistance ELSE LEFT_VALUE END,");
		sql.append("RIGHT_VALUE = CASE WHEN RIGHT_VALUE >= :moveRight THEN RIGHT_VALUE - :moveDistance ELSE RIGHT_VALUE END ");
		sql.append("WHERE RIGHT_VALUE >= :moveRight ");
		
		result+= namedParameterJdbcTemplate.update(sql.toString(),params);
		unlockTables();
		return result;
	}
	
	public Integer moveNodeBefore(Long moveId,Long targetId) {
		return moveNodeBefore(moveId, targetId,true);
	}
	/**
	 * @Title: moveNodeBefore
	 * @Description:  移动菜单到目标菜单前面 
	 * @author xh
	 * @date 2021年5月29日
	 * @param moveId 被移动菜单ID
	 * @param targetId 目标菜单ID
	 * @return
	 */
	@Transactional
	public Integer moveNodeBefore(Long moveId,Long targetId,boolean isUnlockTable ) {
		this.lockTable();
		   
		Menu moveMenu = this.selectById(moveId);
		
		Menu targetMenu = this.selectById(targetId);
		
		Integer nodeLeft = moveMenu.getLeftValue();
		Integer nodeRight = moveMenu.getRightValue();
		
		//要移动菜单的范围，即被移动菜单及其子节点的左右值范围
		Integer nodeDist = nodeRight - nodeLeft+1; //确定要移动的范围
		Integer level = moveMenu.getLevel();
		
		Integer targetLeft = targetMenu.getLeftValue();
		Integer targetLevel = targetMenu.getLevel();
		Integer moveNodeLeft = nodeLeft < targetLeft?nodeLeft:nodeLeft + nodeDist;
		if((targetLeft>=nodeLeft && targetLeft<= nodeRight)||(moveId==targetId)) {
			unlockTables();
			//is child node
			return -1;
		}
		
		Integer result = 0;
		
		StringBuilder sql = new StringBuilder("UPDATE ");
		sql.append(tableName);
		sql.append(" SET LEFT_VALUE = CASE WHEN LEFT_VALUE >= :targetLeft THEN LEFT_VALUE + :nodeDist ELSE LEFT_VALUE END ,");
		sql.append("RIGHT_VALUE = CASE WHEN RIGHT_VALUE >= :targetLeft THEN RIGHT_VALUE + :nodeDist ELSE RIGHT_VALUE END ");
		sql.append("WHERE RIGHT_VALUE >= :targetLeft");
		
		MapSqlParameterSource  params = new MapSqlParameterSource();
		params.addValue("nodeLeft", nodeLeft);
		params.addValue("nodeRight", nodeRight);
		params.addValue("targetLeft", targetLeft);
		params.addValue("nodeDist", nodeDist);
		
		NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		
		result+=namedParameterJdbcTemplate.update(sql.toString(),params);
		
		sql = new StringBuilder("UPDATE ");
		sql.append(tableName);
		sql.append(" SET LEFT_VALUE = LEFT_VALUE + :targetLeft - :moveNodeLeft ,");
		sql.append("RIGHT_VALUE = RIGHT_VALUE + :targetLeft - :moveNodeLeft ,");
		sql.append("LEVEL = LEVEL - :level + :targetLevel WHERE LEFT_VALUE >= :moveNodeLeft AND RIGHT_VALUE <= :moveNodeLeft + :nodeDist -1 ");
		
		params.addValue("moveNodeLeft", moveNodeLeft);
		params.addValue("level", level);
		params.addValue("targetLevel", targetLevel);
		result +=namedParameterJdbcTemplate.update(sql.toString(),params);
		
		sql = new StringBuilder("UPDATE ");
		sql.append(tableName);
		sql.append(" SET LEFT_VALUE = CASE WHEN LEFT_VALUE >= :moveNodeLeft THEN LEFT_VALUE - :nodeDist ELSE LEFT_VALUE END,");
		sql.append("RIGHT_VALUE = CASE WHEN RIGHT_VALUE > :moveNodeLeft THEN RIGHT_VALUE - :nodeDist ELSE RIGHT_VALUE END ");
		sql.append("WHERE RIGHT_VALUE > :moveNodeLeft ");
		
		result+= namedParameterJdbcTemplate.update(sql.toString(),params);
		if(isUnlockTable) {
			unlockTables();
		}
		return result;
	}
	
	@Transactional
	public Integer moveNodeAfter(Long moveId,Long targetId ) {
		int result = this.moveNodeBefore(moveId, targetId,false);
		if(result>0) {
			result += moveNodeBackward(moveId, targetId);
			unlockTables();
		}
		return result;
	}
	
	private int moveNodeBackward(Long moveId,Long targetId) {
		Menu moveMenu = this.selectById(moveId);
		
		Menu targetMenu = this.selectById(targetId);
		
		Integer nodeLeft = moveMenu.getLeftValue();
		Integer nodeRight = moveMenu.getRightValue();
		
		Integer targetLeft = targetMenu.getLeftValue();
		Integer targetRight = targetMenu.getRightValue();
		
		Integer nodeDist = nodeRight - nodeLeft+1; //确定要移动的范围
		
		Integer targetDist = targetRight - targetLeft+1;
		
		List<Long> ids = jdbcTemplate.queryForList("SELECT ID FROM "+tableName+" WHERE LEFT_VALUE >= ? AND RIGHT_VALUE<=?",Long.class,targetLeft,targetRight);
		
		
		StringBuilder sql = new StringBuilder("UPDATE ");
		sql.append(tableName);
		sql.append(" SET LEFT_VALUE = CASE WHEN LEFT_VALUE >= :nodeLeft THEN LEFT_VALUE + :targetDist ELSE LEFT_VALUE END ,");
		sql.append("RIGHT_VALUE = CASE WHEN RIGHT_VALUE <= :nodeRight THEN RIGHT_VALUE + :targetDist ELSE RIGHT_VALUE END ");
		sql.append("WHERE LEFT_VALUE >= :nodeLeft AND RIGHT_VALUE <= :nodeRight");
		
		MapSqlParameterSource  params = new MapSqlParameterSource();
		params.addValue("nodeLeft", nodeLeft);
		params.addValue("nodeRight", nodeRight);
		params.addValue("targetLeft", targetLeft);
		params.addValue("targetRight", targetRight);
		params.addValue("nodeDist", nodeDist);
		params.addValue("targetDist", targetDist);
		params.addValue("ids", ids);
		
		NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		
		int result=namedParameterJdbcTemplate.update(sql.toString(),params);
		
		//System.out.println("target dist:"+targetDist+" node dist:"+nodeDist);
		sql = new StringBuilder("UPDATE ");
		sql.append(tableName);
		sql.append(" SET LEFT_VALUE = LEFT_VALUE - :nodeDist,RIGHT_VALUE = RIGHT_VALUE - :nodeDist ");
		sql.append("WHERE LEFT_VALUE >= :targetLeft AND RIGHT_VALUE <= :targetRight AND ID IN (:ids) ");
		
		result+=namedParameterJdbcTemplate.update(sql.toString(),params);
		
		return result;
	}
	
	public class MenuRowMapper implements  RowMapper<Menu>{

		@Override
		public Menu mapRow(ResultSet rs, int rowNum) throws SQLException {
			Menu menu = new Menu();
			menu.setId(rs.getLong("ID"));
			menu.setCreateDate(rs.getDate("CREATE_DATE"));
			menu.setDescription(rs.getString("DESCRIPTION"));
			menu.setName(rs.getString("NAME"));
			menu.setUrl(rs.getString("URL"));
			menu.setIcon(rs.getString("ICON"));
			menu.setLeftValue(rs.getInt("LEFT_VALUE"));
			menu.setRightValue(rs.getInt("RIGHT_VALUE"));
			menu.setPermissionCodes(rs.getString("PERMISSION_CODES"));
			menu.setProperty(rs.getString("PROPERTY"));
			menu.setLevel(rs.getInt("LEVEL"));
			menu.setUpdateDate(rs.getDate("UPDATE_DATE"));
			return menu;
		}
		
	}
}
