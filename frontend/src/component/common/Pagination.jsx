import React from "react";

// | Props ����      | ����                 | ���� |
// | ------------- | ------------------ | -- |
// | `roomPerPage` | ÿҳ��ʾ���ٸ�����          | ���� |
// | `totalRooms`  | �ܹ��ж��ٸ�����           | ���� |
// | `currentPage` | ��ǰ�ǵڼ�ҳ             | ���� |
// | `paginate`    | ���ҳ��ʱҪ�����ĺ������ı䵱ǰҳ�� | ���� |

const Pagination = ({ roomPerPage, totalRooms, currentPage, paginate }) => {

    const pageNumber = [];
// �����ܷ�������ÿҳ��ʾ����������̬������ҳ��

// �� Math.ceil ��Ϊ������ȡ�������� 19 �����䡢ÿҳ 5 �� �� ��Ҫ 4 ҳ

// Ȼ���ҳ���� pageNumber �����У����� [1, 2, 3, 4]
    for(let i = 1; i <= Math.ceil(totalRooms / roomPerPage); i++){
        pageNumber.push(i);
    }

    return(
        // | ����                                 | ˵��                                             |
// | ---------------------------------- | ---------------------------------------------- |
// | ����ҳ��                               | �� `.map()` ����ҳ������                              |
// | `<li>` + `<button>`                | ÿһҳ����һ����ť                                      |
// | `onClick={() => paginate(number)}` | �����ť�󣬵����ⲿ����� `paginate()` ����������ǰҳ����Ϊ `number` |
// | `className={...}`                  | �����ǰҳ�Ͱ�ťҳ����ȣ����� `current-page` �ࣨ���ڸ�����ǰҳ��      |

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
