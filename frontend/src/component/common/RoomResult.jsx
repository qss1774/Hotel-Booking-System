import React from "react";
import ApiService from "../../service/ApiService";
import { useNavigate } from "react-router-dom";


const RoomResult = ({roomSearchResults}) => {
    const navigate = useNavigate();
    const isAdmin = ApiService.isAdmin();

    return (
        <section className="room-results">

            {/* 如果有搜索结果，就渲染列表；
如果没有，什么也不显示（可以考虑加一个 “暂无数据” 提示） */}
            { roomSearchResults && roomSearchResults.length > 0 && (
            <div className="room-list">
                {/* roomSearchResults.map(...)：遍历房间列表
每个房间显示：图片、房型、价格、描述 */}
                {roomSearchResults.map(room=>(
                    <div className="room-list-item" key={room.id}>
                        <img className="room-list-item-image" src={room.imageUrl} alt={room.roomNumber} />
                        <div className="room-details">
                            <h3>{room.type}</h3>
                            <p>Price: ${room.pricePerNight}/Night</p>
                            <p>Description: {room.description}</p>
                        </div>
{/* 管理员	Edit Room	/admin/edit-room/:id
普通用户	View/Book Now	/room-details/:id */}
                        <div className="book-now-div">
                            {isAdmin ? (
                                <button className="edit-room-button" 
                                onClick={() => navigate(`/admin/edit-room/${room.id}`)}>
                                        Edit Room
                                </button>
                            ): (
                                <button className="book-now-button" 
                                onClick={() => navigate(`/room-details/${room.id}`)}>
                                        View/Book Now
                                </button>

                            )}
                        </div>
                    </div>
                ))}
            </div>
            )}
        </section>
    );

}
export default RoomResult;