package com.sdo.dw.rtc.cleaning.filter.impl;

import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sdo.dw.rtc.cleaning.filter.Filter;
import com.sdo.dw.rtc.cleaning.filter.FilterType;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.ComparisonOperator;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

/**
 * @author xiejing.kane
 *
 */
@FilterType("bool")
public class BoolFilter implements Filter {
	private static Logger LOGGER = LoggerFactory.getLogger(BoolFilter.class);
	private String conditions;

	@Override
	public void init(JSONObject config) {
		conditions = config.getString("conditions");
		LOGGER.info("conditions = " + conditions);
	}

	@Override
	public JSONObject filter(JSONObject source) throws JSQLParserException {
		ExpressionVisitorImpl visitor = new ExpressionVisitorImpl(JSON.parseObject(source.toJSONString()));
		Statement stmt = CCJSqlParserUtil.parse("select * from t where " + conditions);
		Select select = (Select) stmt;
		PlainSelect ps = (PlainSelect) select.getSelectBody();
		ps.getWhere().accept(visitor);
		if (visitor.getResult()) {
			return source;
		}
		return null;
	}

}

class ExpressionVisitorImpl extends ExpressionVisitorAdapter {
	private JSONObject source;
	private Stack<Boolean> stack = new Stack<>();

	public boolean getResult() {
		return stack.pop();
	}

	ExpressionVisitorImpl(JSONObject source) {
		this.source = source;
	}

	private void processComparisonOperator(ComparisonOperator expression, String operator) {
		Expression right = expression.getRightExpression();
		Expression left = expression.getLeftExpression();
		String key = left.toString();
		expression.getLeftExpression().accept(this);
		if (right instanceof StringValue) {
			String value = ((StringValue) right).getValue();
			stack.push(compare(source.getString(key).compareTo(value), 0, operator));
		} else if (right instanceof LongValue) {
			long value = ((LongValue) right).getValue();
			stack.push(compare(source.getLong(key), value, operator));
		} else if (right instanceof DoubleValue) {
			double value = ((DoubleValue) right).getValue();
			stack.push(compare(source.getDouble(key), value, operator));
		}
	}

	boolean compare(double i1, double i2, String operator) {
		if (operator.equals(">")) {
			return i1 > i2;
		} else if (operator.equals(">=")) {
			return i1 >= i2;
		} else if (operator.equals("<")) {
			return i1 < i2;
		} else if (operator.equals("<=")) {
			return i1 <= i2;
		} else if (operator.equals("==")) {
			return i1 == i2;
		} else if (operator.equals("!=")) {
			return i1 != i2;
		} else {
			return false;
		}
	}

	@Override
	public void visit(NotEqualsTo notEqualsTo) {
		processComparisonOperator(notEqualsTo, "!=");
	}

	@Override
	public void visit(MinorThanEquals minorThanEquals) {
		processComparisonOperator(minorThanEquals, "<=");
	}

	@Override
	public void visit(MinorThan minorThan) {
		processComparisonOperator(minorThan, "<");
	}

	@Override
	public void visit(GreaterThanEquals greaterThanEquals) {
		processComparisonOperator(greaterThanEquals, ">=");
	}

	@Override
	public void visit(GreaterThan greaterThan) {
		processComparisonOperator(greaterThan, ">");
	}

	@Override
	public void visit(EqualsTo equalsTo) {
		processComparisonOperator(equalsTo, "==");
	}

	@Override
	public void visit(OrExpression orExpression) {
		orExpression.getLeftExpression().accept(this);
		orExpression.getRightExpression().accept(this);
		stack.push(stack.pop() || stack.pop());
	}

	@Override
	public void visit(AndExpression andExpression) {
		andExpression.getLeftExpression().accept(this);
		andExpression.getRightExpression().accept(this);
		stack.push(stack.pop() && stack.pop());
	}

	@Override
	public void visit(IsNullExpression expr) {
		Expression left = expr.getLeftExpression();
		String key = left.toString();
		// "包含该key" 和 "is null" 异或
		stack.push(source.containsKey(key) ^ !expr.isNot());
	}
}
