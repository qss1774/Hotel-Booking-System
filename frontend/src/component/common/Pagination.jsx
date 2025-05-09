import React from "react";

// | Props 名称      | 含义                 | 类型 |
// | ------------- | ------------------ | -- |
// | `roomPerPage` | 每页显示多少个房间          | 数字 |
// | `totalRooms`  | 总共有多少个房间           | 数字 |
// | `currentPage` | 当前是第几页             | 数字 |
// | `paginate`    | 点击页码时要触发的函数（改变当前页） | 函数 |

const Pagination = ({ roomPerPage, totalRooms, currentPage, paginate }) => {

    const pageNumber = [];
// 根据总房间数和每页显示的数量来动态计算总页数

// 用 Math.ceil 是为了向上取整，比如 19 个房间、每页 5 个 → 需要 4 页

// 然后把页码存进 pageNumber 数组中，例如 [1, 2, 3, 4]
    for(let i = 1; i <= Math.ceil(totalRooms / roomPerPage); i++){
        pageNumber.push(i);
    }

    return(
        // | 功能                                 | 说明                                             |
// | ---------------------------------- | ---------------------------------------------- |
// | 遍历页码                               | 用 `.map()` 遍历页码数组                              |
// | `<li>` + `<button>`                | 每一页生成一个按钮                                      |
// | `onClick={() => paginate(number)}` | 点击按钮后，调用外部传入的 `paginate()` 函数，将当前页更新为 `number` |
// | `className={...}`                  | 如果当前页和按钮页码相等，加上 `current-page` 类（用于高亮当前页）      |

        <div className="pagination-nav">
            <ul className="pagination-ul">
                {pageNumber.map((number)=>(
                    <li key={number} className="pagination-li">
                        <button onClick={()=> paginate(number)} 
                        className={`pagination-button ${currentPage === number ? 'current-page' : ''}`}>
                            {number}
                        </button>
                    </li>
                ))}
            </ul>
        </div>
    )
};

export default Pagination;
