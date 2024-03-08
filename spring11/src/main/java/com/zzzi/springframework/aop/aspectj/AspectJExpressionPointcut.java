package com.zzzi.springframework.aop.aspectj;

import com.zzzi.springframework.aop.ClassFilter;
import com.zzzi.springframework.aop.MethodMatcher;
import com.zzzi.springframework.aop.Pointcut;
import org.aspectj.weaver.tools.PointcutExpression;
import org.aspectj.weaver.tools.PointcutParser;
import org.aspectj.weaver.tools.PointcutPrimitive;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**@author zzzi
 * @date 2023/11/11 14:44
 * 在这里实现匹配器的功能，包括类匹配器和方法匹配器
 */
public class AspectJExpressionPointcut implements Pointcut, ClassFilter, MethodMatcher {
    /**@author zzzi
     * @date 2023/11/11 14:45
     * 代表支持的切点原语是什么
     */
    private static final Set<PointcutPrimitive> SUPPORTED_PRIMITIVES=new HashSet<>();
    static {
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.EXECUTION);
    }

    /**@author zzzi
     * @date 2024/3/8 14:28
     * 根据传入的切点表达式得到切点表达式的解析器
     * 之后使用这个解析器判断当前类中是否有方法需要被增强
     * 当前类中的哪些方法需要被增强
     */
    private final PointcutExpression pointcutExpression;
    public AspectJExpressionPointcut(String expression) {
        PointcutParser pointcutParser = PointcutParser.
                getPointcutParserSupportingSpecifiedPrimitivesAndUsingSpecifiedClassLoaderForResolution(SUPPORTED_PRIMITIVES,
                        this.getClass().getClassLoader());
        pointcutExpression = pointcutParser.parsePointcutExpression(expression);
    }
    /**@author zzzi
     * @date 2023/11/11 14:50
     * 实现类匹配器和方法匹配器
     */
    @Override
    public boolean matches(Class<?> clazz) {
        return pointcutExpression.couldMatchJoinPointsInType(clazz);
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        return pointcutExpression.matchesMethodExecution(method).alwaysMatches();
    }

    @Override
    public ClassFilter getClassFilter() {
        return this;
    }

    @Override
    public MethodMatcher getMethodMatcher() {
        return this;
    }
}
